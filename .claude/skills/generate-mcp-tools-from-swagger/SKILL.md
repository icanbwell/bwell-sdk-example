---
name: generate-mcp-tools-from-swagger
description: Given a Swagger/OpenAPI URL, generates a Makefile target using openapi-python-client, runs it to produce a Python client, and creates MCP tools for the main capabilities in that client.
when_to_use: When user asks to create MCP tools from a Swagger/OpenAPI spec, add a new REST API service integration, or scaffold MCP tools from an OpenAPI schema.
argument-hint: "<swagger-url> <service-name> [--auth-type <bearer|apikey|basic>]"
disable-model-invocation: true
user-invocable: true
allowed-tools: Read Write Edit Bash Grep Glob
effort: high
---

# Generate MCP Tools from a Swagger/OpenAPI Server

You are generating a complete MCP tool integration from a Swagger/OpenAPI specification URL. This skill scaffolds:
1. A Makefile target for OpenAPI client generation using `openapi-python-client`
2. Configuration for the code generator
3. A Python client wrapper (retriever)
4. Pydantic result models
5. FastMCP tool registration
6. Unit tests

## Arguments

Parse `$ARGUMENTS` as:
- **First positional arg**: The Swagger/OpenAPI spec URL (e.g., `https://api.example.com/swagger.json` or `https://api.example.com/openapi.yaml`)
- **Second positional arg**: The service name in kebab-case (e.g., `lab-results`)
- **Optional `--auth-type`**: Authentication type — `bearer` (default), `apikey`, or `basic`

Derive these from the service name:
- `snake_name`: kebab-case → snake_case (e.g., `lab_results`)
- `PascalName`: kebab-case → PascalCase (e.g., `LabResults`)
- `client_package`: `{snake_name}_client`

## Reference Implementation

Use `mcpfhiragent/mcp_servers/tools/thedacare_scheduling/` as the structural pattern (adapted for REST):
- `mcpfhiragent/mcp_servers/{snake_name}/generated/{client_package}/` — auto-generated client code
- `mcpfhiragent/mcp_servers/{snake_name}/code_generation/openapi-config.yaml` — generator config
- `mcpfhiragent/mcp_servers/tools/{snake_name}/register.py` — tool registration
- `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_retriever.py` — business logic
- `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_results.py` — result models
- `tests/mcp_servers/tools/{snake_name}/` — unit tests

## Execution Steps

### Step 1: Fetch and Analyze the OpenAPI Spec

Download and inspect the spec to understand available endpoints:

```bash
curl -s "{swagger_url}" | python3 -m json.tool > /tmp/openapi-spec.json
```

Or for YAML:
```bash
curl -s "{swagger_url}" > /tmp/openapi-spec.yaml
```

Identify:
- Available API paths (endpoints)
- HTTP methods per path (GET, POST, PUT, DELETE)
- Request/response schemas
- Authentication requirements
- Main CRUD operations and business capabilities

Group endpoints by capability (e.g., "appointments", "patients", "orders") to determine which MCP tools to create. Each MCP tool should map to one logical operation, not necessarily one endpoint.

### Step 2: Create the Code Generation Directory Structure

```
mcpfhiragent/mcp_servers/{snake_name}/
├── __init__.py
├── code_generation/
│   └── openapi-config.yaml
└── generated/
    └── (will be populated by codegen)
```

### Step 3: Create `openapi-config.yaml`

```yaml
# openapi-python-client configuration
# See: https://github.com/openapi-generators/openapi-python-client
project_name_override: "{client_package}"
package_name_override: "{client_package}"
```

### Step 4: Add Makefile Target

Add a target to the project Makefile:

```makefile
.PHONY: openapi-generate-{service-name}
openapi-generate-{service-name}: ## Generate Python client from {service-name} OpenAPI spec
	@echo "Generating Python client from {service-name} OpenAPI spec..."
	rm -rf ./mcpfhiragent/mcp_servers/{snake_name}/generated/{client_package} && \
	docker run --rm \
		-v $$(pwd)/mcpfhiragent/mcp_servers/{snake_name}/generated:/generated \
		-v $$(pwd)/mcpfhiragent/mcp_servers/{snake_name}/code_generation/openapi-config.yaml:/config/openapi-config.yaml:ro \
		python:3.12-slim bash -c "\
			pip install -q openapi-python-client && \
			cd /generated && \
			openapi-python-client generate \
				--url '{swagger_url}' \
				--config /config/openapi-config.yaml \
				--output-path /generated/{client_package} \
				--overwrite"
	@echo "✅ Python client generated for {service-name}"
```

Insert it near the other code generation targets in the Makefile.

### Step 5: Run Code Generation

Execute the Makefile target to generate the client:

```bash
make openapi-generate-{service-name}
```

If it fails (auth required, URL unreachable), inform the user and show what parameters need to be set.

### Step 6: Analyze Generated Client

After generation, examine the generated client to understand:
- Available API classes and methods
- Request/response model classes
- Authentication configuration
- Error handling patterns

The generated client will typically have:
```
generated/{client_package}/
├── __init__.py
├── client.py          # Main client class
├── api/               # API operation modules
│   ├── __init__.py
│   └── {resource}/    # One module per API tag/resource
├── models/            # Request/response Pydantic models
│   ├── __init__.py
│   └── *.py
└── types.py           # Shared types
```

### Step 7: Create Result Models

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_results.py`:

```python
from typing import Optional
from pydantic import BaseModel, Field
from mcpfhiragent.mcp_servers.tools.structures.base_mcp_result import BaseMcpResult

class {PascalName}Result(BaseMcpResult):
    # Map the most important fields from API responses
    # Do NOT expose raw API models directly — create focused result types
    pass
```

Result model design principles:
- Only expose fields that are useful for the AI agent and user
- Flatten deeply nested structures where possible
- Use Optional for all fields (API responses can be partial)
- Translate internal codes to human-readable descriptions where applicable

### Step 8: Create the Retriever

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_retriever.py`:

```python
import logging
from typing import Optional
from fastmcp import Context
from fastmcp.exceptions import ToolError
from httpx import HTTPStatusError

from mcpfhiragent.utilities.environment.mcp_fhir_agent_environment_variables import (
    McpFhirAgentEnvironmentVariables,
)

logger = logging.getLogger(__name__)


class {PascalName}Retriever:
    def __init__(self, *, environment_variables: McpFhirAgentEnvironmentVariables) -> None:
        if environment_variables is None:
            raise ValueError("environment_variables cannot be None")
        self.environment_variables = environment_variables

    def _get_client(self):
        \"\"\"Instantiate the generated API client with auth.\"\"\"
        from mcpfhiragent.mcp_servers.{snake_name}.generated.{client_package}.client import Client
        # Configure based on auth_type
        return Client(base_url="...", headers={"Authorization": f"Bearer {token}"})

    async def operation_name(self, *, param: str, ctx: Context) -> ResultType:
        logger.info("Performing operation...")
        await ctx.info("Fetching data...")

        client = self._get_client()
        try:
            response = await client.api_method(param=param)
        except HTTPStatusError as e:
            await ctx.error("Operation failed")
            raise ToolError(f"Request failed: HTTP {e.response.status_code}") from e

        # Map response to result model
        return ResultType(...)
```

Key patterns:
- The retriever wraps the generated client, handling auth and error mapping
- Use `httpx` for HTTP operations (the generated client uses httpx internally)
- Map exceptions to `ToolError` with clear messages
- Use `ctx.info()` / `ctx.error()` for progress reporting
- If the service requires token exchange or caching, follow the `ThedacareAuthCache` pattern

### Step 9: Create Tool Registration

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/register.py`:

```python
import logging
from typing import Any, Annotated, Optional
from fastmcp import FastMCP, Context
from pydantic import Field
from mcpfhiragent.mcp_servers.tools.tool_dependencies import ToolDependencies
from mcpfhiragent.mcp_servers.tools.{snake_name}.{snake_name}_retriever import {PascalName}Retriever
from mcpfhiragent.mcp_servers.tools.{snake_name}.{snake_name}_results import {PascalName}Result

logger = logging.getLogger(__name__)

def register(mcp: FastMCP[Any], deps: ToolDependencies) -> None:
    environment_variables = deps.environment_variables

    @mcp.tool(name="{tool_name}", description=TOOL_DESCRIPTION)
    async def tool_function(
        *,
        param: Annotated[str, Field(description="Parameter description.")],
        ctx: Context,
    ) -> {PascalName}Result:
        retriever = {PascalName}Retriever(environment_variables=environment_variables)
        return await retriever.operation(param=param, ctx=ctx)
```

Tool naming convention: `{action}_{service}_{resource}` (e.g., `get_lab_results_list`, `submit_lab_order`)

### Step 10: Add Tool Descriptions to Structures

Add tool description constants to `mcpfhiragent/mcp_servers/tools/fhir_record/structures/structures.py`:

```python
{UPPER_SNAKE_TOOL_NAME} = (
    "One-sentence description of what the tool does. "
    "When to use: describe the scenario. "
    "Returns: what the response contains. "
    "Prerequisites: any tools that should be called first."
)
```

### Step 11: Register Tools in Registry

Update `mcpfhiragent/mcp_servers/tools/registry.py`:

```python
from mcpfhiragent.mcp_servers.tools.{snake_name}.register import (
    register as register_{snake_name},
)

def register_all_tools(mcp: FastMCP[Any], deps: ToolDependencies) -> None:
    # ... existing registrations ...
    register_{snake_name}(mcp, deps)
```

### Step 12: Create Unit Tests

Create `tests/mcp_servers/tools/{snake_name}/test_{snake_name}_retriever.py`:

```python
from unittest.mock import AsyncMock, MagicMock
import httpx
import pytest
import respx
from fastmcp.exceptions import ToolError
from mcpfhiragent.mcp_servers.tools.{snake_name}.{snake_name}_retriever import {PascalName}Retriever

BASE_URL = "https://api.example.com"

@pytest.fixture()
def mock_env() -> MagicMock:
    env = MagicMock()
    env.{snake_name}_base_url = BASE_URL
    # Add other required env vars
    return env

@pytest.fixture()
def retriever(mock_env: MagicMock) -> {PascalName}Retriever:
    return {PascalName}Retriever(environment_variables=mock_env)

@pytest.fixture()
def mock_ctx() -> AsyncMock:
    ctx = AsyncMock()
    ctx.info = AsyncMock()
    ctx.error = AsyncMock()
    return ctx


class TestOperation:
    @respx.mock
    @pytest.mark.asyncio
    async def test_success(self, retriever, mock_ctx) -> None:
        respx.get(f"{BASE_URL}/endpoint").mock(
            return_value=httpx.Response(200, json={"key": "value"})
        )
        result = await retriever.operation(param="test", ctx=mock_ctx)
        assert result.field == "value"

    @respx.mock
    @pytest.mark.asyncio
    async def test_http_error(self, retriever, mock_ctx) -> None:
        respx.get(f"{BASE_URL}/endpoint").mock(
            return_value=httpx.Response(500, json={"error": "internal"})
        )
        with pytest.raises(ToolError, match="HTTP 500"):
            await retriever.operation(param="test", ctx=mock_ctx)
```

### Step 13: Create `__init__.py` Files

Create empty `__init__.py` files in:
- `mcpfhiragent/mcp_servers/{snake_name}/`
- `mcpfhiragent/mcp_servers/{snake_name}/code_generation/`
- `mcpfhiragent/mcp_servers/tools/{snake_name}/`
- `tests/mcp_servers/tools/{snake_name}/`

## Key Differences from GraphQL Skill

| Aspect | GraphQL | OpenAPI/Swagger |
|--------|---------|-----------------|
| Code generator | ariadne-codegen | openapi-python-client |
| Config format | TOML | YAML |
| Query definition | `.graphql` files | Derived from spec |
| Client style | Single client with typed methods | API modules per resource |
| Error handling | `GraphQLClientGraphQLMultiError` | `HTTPStatusError` |
| Auth in codegen | Header in config | Not needed (runtime only) |

## Verification Checklist

After completing all steps, verify:
- [ ] OpenAPI spec is accessible and valid
- [ ] Makefile target is syntactically correct and follows existing conventions
- [ ] `openapi-config.yaml` has correct package/project name overrides
- [ ] Generated client code exists in `generated/{client_package}/`
- [ ] Result models expose useful fields without leaking raw API internals
- [ ] Retriever handles authentication and maps errors to `ToolError`
- [ ] Tool registration uses proper FastMCP patterns with `Annotated` + `Field`
- [ ] Tool descriptions are meaningful for the AI agent
- [ ] Registry.py imports and calls the new register function
- [ ] Unit tests cover success paths, error paths, and empty responses
- [ ] All `__init__.py` files exist
- [ ] No PHI/PII in test fixtures or example data
- [ ] Syntax check passes for all new Python files

## Notes on openapi-python-client

- GitHub: https://github.com/openapi-generators/openapi-python-client
- Generates async httpx-based clients by default
- Supports OpenAPI 3.0 and 3.1 (and Swagger 2.0 via conversion)
- For Swagger 2.0 URLs, the tool auto-converts; if not, use `swagger2openapi` first
- Generated models use `attrs` by default; if you need Pydantic, pass `--custom-template-path` or post-process
- If the spec has circular references or unusual patterns, you may need to add `field_overrides` in the config