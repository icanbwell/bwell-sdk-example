---
name: create-mcp-tool
description: "Scaffold a new MCP tool in this repo. Generates the tool directory with register.py, retriever, result model, __init__.py, and wires it into the central registry. Use when someone says 'create a new tool', 'add an MCP tool', 'scaffold a tool', or invokes /create-mcp-tool."
when_to_use: "When a developer wants to add a new MCP tool. Triggers on: /create-mcp-tool, 'create a new tool', 'add MCP tool', 'scaffold tool', 'new tool for <capability>'."
disable-model-invocation: true
user-invocable: true
allowed-tools: Read Write Edit Bash Grep Glob
argument-hint: "<tool_name> [--simple] [--description='...'] [--params='param1:type,param2:type']"
effort: low
---

# Create MCP Tool

Scaffold a new MCP tool in this repository.

## Context

- Working directory: !`pwd`
- Current tools: !`ls mcpfhiragent/mcp_servers/tools/`
- Registry contents: !`cat mcpfhiragent/mcp_servers/tools/registry.py`

## Arguments

User provided: $ARGUMENTS

Parse the following from `$ARGUMENTS`:

| Argument | Required | Description |
|----------|----------|-------------|
| `<tool_name>` | Yes | Snake_case name for the tool (e.g., `appointment_reminders`) |
| `--simple` | No | Generate a stateless tool without retriever class (like `date_utils`) |
| `--description='...'` | No | Tool description for the `@mcp.tool()` decorator |
| `--params='...'` | No | Comma-separated parameters as `name:type` (e.g., `person_id:str,start_date:date`) |

If no `<tool_name>` is provided, ask the user what the tool should do and derive the name.

## Pre-flight Checks

1. **Validate name**: Ensure `<tool_name>` is snake_case, does not conflict with an existing tool directory.
2. **Confirm with user**: Show what will be created and ask for confirmation before writing files.

## File Generation

### Directory Structure

Create: `mcpfhiragent/mcp_servers/tools/<tool_name>/`

### File 1: `__init__.py`

Empty file.

### File 2: `<tool_name>_result.py`

Skip this file if `--simple` is set.

```python
from typing import Optional

from pydantic import Field

from mcpfhiragent.mcp_servers.tools.structures.base_mcp_result import BaseMcpResult


class <ToolNamePascal>Result(BaseMcpResult):
    result: Optional[str] = Field(
        default=None,
        description="<description of the result content>",
    )
```

Rules:
- Class name is PascalCase version of `<tool_name>` + `Result`
- Extend `BaseMcpResult` (provides `result`, `error`, `urls`, `meta`, `debug` fields)
- Override `result` field with a more specific description
- Add additional fields only if the user specifies complex return data

### File 3: `<tool_name>_retriever.py`

Skip this file if `--simple` is set.

```python
import logging
from typing import Optional

from fastmcp import Context
from oidcauthlib.auth.models.token import Token
from opentelemetry import trace

from mcpfhiragent.fhir.fhir_client_factory import FhirClientFactoryProtocol
from mcpfhiragent.mcp_servers.tools.<tool_name>.<tool_name>_result import <ToolNamePascal>Result
from mcpfhiragent.utilities.environment.mcp_fhir_agent_environment_variables import (
    McpFhirAgentEnvironmentVariables,
)

logger = logging.getLogger(__name__)
tracer = trace.get_tracer(__name__)


class <ToolNamePascal>Retriever:
    def __init__(
        self,
        *,
        fhir_client_factory: FhirClientFactoryProtocol,
        environment_variables: McpFhirAgentEnvironmentVariables,
    ) -> None:
        self.fhir_client_factory = fhir_client_factory
        self.environment_variables = environment_variables

    async def retrieve(
        self,
        *,
        person_id: str,
        access_token: Optional[Token],
        debug: bool = False,
        ctx: Context,
    ) -> <ToolNamePascal>Result:
        # TODO: Implement retrieval logic
        raise NotImplementedError("Implement retrieve() for <tool_name>")
```

Rules:
- Class name is PascalCase version of `<tool_name>` + `Retriever`
- Constructor takes `fhir_client_factory` and `environment_variables` (standard DI pattern)
- Add/remove constructor params based on what the tool needs (e.g., remove FHIR client if not accessing FHIR data)
- Method signature should match what `register.py` will call
- Include OpenTelemetry tracer for observability
- Adjust parameters based on `--params` flag

### File 4: `register.py`

**Standard tool (with retriever):**

```python
import logging
from typing import Any, Annotated, Optional

from fastmcp import FastMCP, Context
from oidcauthlib.auth.models.token import Token
from pydantic import Field

from mcpfhiragent.mcp_servers.tools.<tool_name>.<tool_name>_result import (
    <ToolNamePascal>Result,
)
from mcpfhiragent.mcp_servers.tools.<tool_name>.<tool_name>_retriever import (
    <ToolNamePascal>Retriever,
)
from mcpfhiragent.mcp_servers.tools.tool_dependencies import ToolDependencies
from mcpfhiragent.mcp_servers.tools.tool_helpers import get_current_access_token_async

logger = logging.getLogger(__name__)


def register(mcp: FastMCP[Any], deps: ToolDependencies) -> None:
    container = deps.container
    token_reader = deps.token_reader

    @mcp.tool(
        name="<tool_name>",
        description="<tool description>",
    )
    async def <tool_name>(
        *,
        person_id: Annotated[
            Optional[str],
            Field(
                description="The person ID. If not provided, extracted from the access token.",
                default=None,
            ),
        ] = None,
        debug: Annotated[
            bool,
            Field(
                description="Whether to display debugging information. Default is False.",
                default=False,
            ),
        ] = False,
        ctx: Context,
    ) -> <ToolNamePascal>Result:
        access_token: Optional[Token] = await get_current_access_token_async(
            ctx=ctx, token_reader=token_reader
        )

        resolved_person_id: Optional[str] = person_id
        if not resolved_person_id and access_token and access_token.claims:
            resolved_person_id = access_token.claims.get("clientFhirPersonId")

        if not resolved_person_id:
            return <ToolNamePascal>Result(
                result=None,
                error="Person ID is required and could not be extracted from the access token.",
            )

        return await container.resolve(<ToolNamePascal>Retriever).retrieve(
            person_id=resolved_person_id,
            access_token=access_token,
            debug=debug,
            ctx=ctx,
        )
```

**Simple tool (stateless, no retriever — when `--simple` is set):**

```python
from typing import Any, Annotated

from fastmcp import FastMCP
from pydantic import Field

from mcpfhiragent.mcp_servers.tools.tool_dependencies import ToolDependencies


def register(mcp: FastMCP[Any], deps: ToolDependencies) -> None:
    @mcp.tool(
        name="<tool_name>",
        description="<tool description>",
    )
    async def <tool_name>(
        *,
        # Add parameters based on --params flag
        debug: Annotated[
            bool,
            Field(
                description="Whether to display debugging information. Default is False.",
                default=False,
            ),
        ] = False,
    ) -> str:
        # TODO: Implement tool logic
        raise NotImplementedError("Implement <tool_name>")
```

Rules for `register.py`:
- The `register` function signature is always `def register(mcp: FastMCP[Any], deps: ToolDependencies) -> None`
- Tool function uses keyword-only args (`*` after self)
- All parameters use `Annotated[type, Field(description=...)]` pattern
- `ctx: Context` is always the last parameter (omit for simple tools that don't need auth)
- `debug` parameter is standard on all tools
- Person ID resolution from access token is the standard pattern for patient-scoped tools
- If `--params` is provided, generate appropriate parameters instead of the default `person_id`

## Registry Wiring

After generating files, update `mcpfhiragent/mcp_servers/tools/registry.py`:

1. **Add import** (alphabetical by tool name, following existing style):
```python
from mcpfhiragent.mcp_servers.tools.<tool_name>.register import (
    register as register_<tool_name>,
)
```

2. **Add call** in `register_all_tools()` (at the end, before closing):
```python
register_<tool_name>(mcp, deps)
```

## DI Container Wiring

Skip this section if `--simple` is set.

After updating the registry, register the retriever in `mcpfhiragent/container/mcp_fhir_agent_container_factory.py`:

1. **Add import** at the top of the file:
```python
from mcpfhiragent.mcp_servers.tools.<tool_name>.<tool_name>_retriever import (
    <ToolNamePascal>Retriever,
)
```

2. **Add singleton registration** inside the container setup (alongside existing retriever registrations):
```python
container.singleton(
    <ToolNamePascal>Retriever,
    lambda c: <ToolNamePascal>Retriever(
        environment_variables=c.resolve(McpFhirAgentEnvironmentVariables),
        fhir_client_factory=c.resolve(FhirClientFactoryProtocol),
    ),
)
```

Adjust the lambda arguments to match the retriever's `__init__` parameters. If the retriever doesn't need `fhir_client_factory`, remove it from both the retriever constructor and the DI registration.

## Post-Generation

After creating all files:

1. **Verify imports**: Run `python -c "from mcpfhiragent.mcp_servers.tools.<tool_name>.register import register"` to confirm no import errors.
2. **Show summary**: Print what was created:

```
MCP Tool scaffolded: <tool_name>
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Created:
  ✓ mcpfhiragent/mcp_servers/tools/<tool_name>/__init__.py
  ✓ mcpfhiragent/mcp_servers/tools/<tool_name>/<tool_name>_result.py      (omitted if --simple)
  ✓ mcpfhiragent/mcp_servers/tools/<tool_name>/<tool_name>_retriever.py   (omitted if --simple)
  ✓ mcpfhiragent/mcp_servers/tools/<tool_name>/register.py

Updated:
  ✓ mcpfhiragent/mcp_servers/tools/registry.py
  ✓ mcpfhiragent/container/mcp_fhir_agent_container_factory.py            (omitted if --simple)

Next steps:
  1. Implement the retrieval logic in <tool_name>_retriever.py
  2. Write tests in tests/mcp_servers/tools/<tool_name>/
```

When `--simple` is set, omit the result and retriever lines from the summary and adjust next steps to only mention implementing logic in `register.py`.

3. **Offer to generate tests**: Ask if the user wants a test file scaffolded at `tests/mcp_servers/tools/<tool_name>/test_<tool_name>_retriever.py`.

## Important Rules

- Always use the exact import patterns from the existing codebase (check registry.py for style).
- Tool names in the `@mcp.tool(name=...)` decorator use snake_case.
- Class names use PascalCase.
- Never generate placeholder PHI/PII in examples or test data.
- If the user describes a tool that overlaps with an existing one, warn them and suggest extending the existing tool instead.
- Respect the DI pattern — retrievers get dependencies via constructor injection, not global imports.
- Include OpenTelemetry tracing in retrievers for observability compliance.
