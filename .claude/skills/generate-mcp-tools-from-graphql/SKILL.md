---
name: generate-mcp-tools-from-graphql
description: Given a GraphQL server URL, generates a Makefile target for code generation, runs ariadne-codegen to produce a Python client, and creates MCP tools for the main capabilities in that client.
when_to_use: When user asks to create MCP tools from a GraphQL API, add a new GraphQL-based service integration, or scaffold MCP tools from a GraphQL schema.
argument-hint: "<graphql-url> <service-name> [--auth-header <header>]"
disable-model-invocation: true
user-invocable: true
allowed-tools: Read Write Edit Bash Grep Glob
effort: high
---

# Generate MCP Tools from a GraphQL Server

You are generating a complete MCP tool integration from a GraphQL server URL. This skill scaffolds:
1. A Makefile target for GraphQL code generation
2. The ariadne-codegen configuration
3. GraphQL queries/mutations from the schema
4. A Python client wrapper (retriever)
5. Pydantic result models
6. FastMCP tool registration
7. Unit tests

## Arguments

Parse `$ARGUMENTS` as:
- **First positional arg**: The GraphQL server URL (e.g., `https://api.example.com/graphql`)
- **Second positional arg**: The service name in kebab-case (e.g., `pharmacy-orders`)
- **Optional `--auth-header`**: The authorization header format (default: `Authorization: Bearer $AUTH_TOKEN`)

Derive these from the service name:
- `snake_name`: kebab-case → snake_case (e.g., `pharmacy_orders`)
- `PascalName`: kebab-case → PascalCase (e.g., `PharmacyOrders`)
- `client_class`: `{PascalName}Client`
- `package_name`: `{snake_name}_client`

## Reference Implementation

Use `mcpfhiragent/mcp_servers/thedacare_scheduling/` as the canonical pattern:
- `code_generation/graphql-codegen.toml` — ariadne-codegen configuration
- `code_generation/queries/*.graphql` — GraphQL operations
- `generated/{package_name}/` — auto-generated client code
- `mcpfhiragent/mcp_servers/tools/{snake_name}/register.py` — tool registration
- `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_retriever.py` — business logic
- `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_results.py` — result models
- `tests/mcp_servers/tools/{snake_name}/` — unit tests

## Execution Steps

### Step 1: Introspect the GraphQL Schema

Use the GraphQL introspection query to discover available queries and mutations:

```bash
curl -s -X POST "{graphql_url}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -d '{"query": "{ __schema { queryType { fields { name description args { name type { name kind ofType { name kind } } } } } mutationType { fields { name description args { name type { name kind ofType { name kind } } } } } } }"}' | python3 -m json.tool
```

If introspection is disabled, ask the user for available operations or documentation.

### Step 2: Create the Code Generation Directory Structure

```
mcpfhiragent/mcp_servers/{snake_name}/
├── __init__.py
├── code_generation/
│   ├── graphql-codegen.toml
│   └── queries/
│       └── {snake_name}.graphql
└── generated/
    └── (will be populated by codegen)
```

### Step 3: Create `graphql-codegen.toml`

```toml
[tool.ariadne-codegen]
remote_schema_url = "{graphql_url}"
queries_path = "/graphql/queries"
target_package_path = "/generated"
target_package_name = "{package_name}"
client_name = "{client_class}"
client_file_name = "{snake_name}_client"
enable_custom_operations = true
remote_schema_headers = {"{auth_header_key}" = "${auth_header_env_var}"}
```

### Step 4: Write GraphQL Queries

Based on the introspected schema, write GraphQL queries and mutations in `code_generation/queries/{snake_name}.graphql`. Focus on the main capabilities:
- List/search operations (queries)
- Get-by-ID operations (queries)
- Create/update/delete operations (mutations)

Follow this pattern for each operation:
```graphql
query operationName($param1: Type!, $param2: Type) {
  fieldName(input: {param1: $param1, param2: $param2}) {
    field1
    field2
    nestedObject {
      nestedField
    }
  }
}
```

### Step 5: Add Makefile Target

Add a target to the project Makefile following the existing pattern:

```makefile
.PHONY: graphql-generate-{service-name}
graphql-generate-{service-name}: ## Generate GraphQL types from {service-name} schema
	@echo "Generating GraphQL types from {service-name} schema..."
	@echo "IMPORTANT: Set AUTH_TOKEN environment variable via export AUTH_TOKEN=xxx before running this."
	rm -rf ./mcpfhiragent/mcp_servers/{snake_name}/generated/{package_name} && \
	docker run --rm \
		-e AUTH_TOKEN \
        -v $$(pwd)/mcpfhiragent/mcp_servers/{snake_name}/generated:/generated \
        -v $$(pwd)/mcpfhiragent/mcp_servers/{snake_name}/code_generation/graphql-codegen.toml:/graphql/graphql-codegen.toml:ro \
		-v $$(pwd)/mcpfhiragent/mcp_servers/{snake_name}/code_generation/queries:/graphql/queries:ro \
        python:3.12-slim bash -c "pip install -q ariadne-codegen>=0.16.0 requests && cd /generated && ariadne-codegen --config /graphql/graphql-codegen.toml"
	@echo "✅ GraphQL types and classes generated for {service-name}"
```

Insert it near the other `graphql-generate-*` targets in the Makefile.

### Step 6: Run Code Generation

Execute the Makefile target to generate the client:

```bash
make graphql-generate-{service-name}
```

If AUTH_TOKEN is not set, inform the user they need to set it and show what the generated output will look like.

### Step 7: Create Result Models

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_results.py`:

```python
from typing import Optional
from pydantic import BaseModel, Field
from mcpfhiragent.mcp_servers.tools.structures.base_mcp_result import BaseMcpResult

class {PascalName}Result(BaseMcpResult):
    # Map fields from the GraphQL response to Pydantic models
    pass
```

Each result model should:
- Extend `BaseMcpResult` for top-level tool results
- Extend `BaseModel` for nested objects
- Use `Optional[T]` with `Field(default=None, description="...")` for all fields
- Use snake_case field names (mapped from camelCase GraphQL fields)

### Step 8: Create the Retriever

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/{snake_name}_retriever.py`:

Follow the pattern from `thedacare_scheduling_retriever.py`:
- Constructor takes `environment_variables: McpFhirAgentEnvironmentVariables`
- Private `_get_graphql_client()` method for client instantiation
- One async method per tool operation
- Each method:
  - Logs the operation
  - Uses `ctx.info()` for progress updates
  - Wraps GraphQL calls in try/except for `GraphQLClientHttpError` and `GraphQLClientGraphQLMultiError`
  - Returns typed result models
  - Uses `ToolError` for error propagation

### Step 9: Create Tool Registration

Create `mcpfhiragent/mcp_servers/tools/{snake_name}/register.py`:

```python
import logging
from typing import Any, Annotated, Optional
from fastmcp import FastMCP, Context
from pydantic import Field
from mcpfhiragent.mcp_servers.tools.tool_dependencies import ToolDependencies

logger = logging.getLogger(__name__)

def register(mcp: FastMCP[Any], deps: ToolDependencies) -> None:
    environment_variables = deps.environment_variables

    @mcp.tool(name="{tool_name}", description=TOOL_DESCRIPTION)
    async def tool_function(*, param: Annotated[Type, Field(description="...")], ctx: Context) -> ResultType:
        retriever = {PascalName}Retriever(environment_variables=environment_variables)
        return await retriever.method(param=param, ctx=ctx)
```

### Step 10: Add Tool Descriptions to Structures

Add tool description constants to `mcpfhiragent/mcp_servers/tools/fhir_record/structures/structures.py`. Each description should explain:
- What the tool does
- When to use it
- What it returns
- Any prerequisites (e.g., "Use X first to get required IDs")

### Step 11: Register Tools in Registry

Update `mcpfhiragent/mcp_servers/tools/registry.py` to import and call the new registration function.

### Step 12: Create Unit Tests

Create `tests/mcp_servers/tools/{snake_name}/test_{snake_name}_retriever.py` following the pattern from `test_thedacare_scheduling_retriever.py`:
- Use `respx.mock` to intercept HTTP calls
- Create fixtures for mock environment and context
- Test success paths with realistic GraphQL responses
- Test error paths (GraphQL errors, HTTP errors)
- Test edge cases (empty results, null fields)

### Step 13: Create `__init__.py` Files

Create empty `__init__.py` files in:
- `mcpfhiragent/mcp_servers/{snake_name}/`
- `mcpfhiragent/mcp_servers/tools/{snake_name}/`
- `tests/mcp_servers/tools/{snake_name}/`

## Verification Checklist

After completing all steps, verify:
- [ ] `graphql-codegen.toml` points to the correct URL and uses proper auth header
- [ ] GraphQL queries cover the main operations from the schema
- [ ] Makefile target runs successfully (or would with AUTH_TOKEN set)
- [ ] Generated client code exists in `generated/{package_name}/`
- [ ] Result models map all important fields from GraphQL responses
- [ ] Retriever class handles auth, errors, and maps responses to result models
- [ ] Tool registration uses proper FastMCP decorators with Annotated parameters
- [ ] Tool descriptions are added to structures.py
- [ ] Registry.py imports and calls the new register function
- [ ] Unit tests cover success and error paths for each operation
- [ ] All `__init__.py` files exist
- [ ] No PHI/PII in test fixtures
- [ ] Syntax check passes: `python -c "import ast; ast.parse(open('path').read())"`