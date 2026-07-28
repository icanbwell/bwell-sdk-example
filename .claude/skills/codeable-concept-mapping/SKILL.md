---
name: codeable-concept-mapping
description: >
  Update the CodeableConcept field mapping generator script in the sensitive-data-tagger package.
  Use when modifying the list of target FHIR resources, adjusting depth, adding/removing field types,
  or changing the output format. Also use when the FHIR spec XSD files are updated.
  Triggers on: "update mapping", "add resource to mapping", "regenerate codeable concept",
  "update sensitive fields", "change mapping script".
---

# CodeableConcept Field Mapping Generator

## What It Does

`packages/sensitive-data-tagger/generatorScripts/generate_codeable_concept_mapping.py` generates a TypeScript file that maps FHIR resource types to their CodeableConcept and Coding field paths. These are fields that can carry sensitive clinical codes (CPT, HCPCS, ICD, SNOMED, LOINC, NDC, RxNorm).

## How It Works

1. Uses `fhir_xml_schema_parser.py` to parse the FHIR R4B (v4.3.0) XML spec (`xsd/definitions.xml/`)
2. For each target resource, recursively traverses properties up to `MAX_DEPTH` levels
3. Collects fields where type is `CodeableConcept` or `Coding`
4. Resolves reusable complex types (e.g., `Dosage.route` inside `MedicationRequest.dosageInstruction`)
5. Outputs a TypeScript file with `export const SensitiveCodeableConceptFieldMapping`

## Key Files

| File                                                                                   | Purpose                                                 |
| -------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| `packages/sensitive-data-tagger/generatorScripts/generate_codeable_concept_mapping.py` | The generator script                                    |
| `packages/sensitive-data-tagger/generatorScripts/fhir_xml_schema_parser.py`            | FHIR XML schema parser (from fhir-server, do not edit)  |
| `packages/sensitive-data-tagger/generatorScripts/xsd/`                                 | FHIR R4B XSD spec files (from fhir-server, do not edit) |
| `packages/sensitive-data-tagger/src/sensitiveCodeableConceptFieldMapping.generated.ts` | Generated output (do not hand-edit)                     |

## Regenerating

```bash
cd packages/sensitive-data-tagger
make generate-codeable-concept-mapping
```

Requires: Docker (the Makefile runs the Python script inside a `python:3.12-alpine` container that auto-installs `lxml`). No local Python setup needed.

## Script Configuration

These are at the top of `generate_codeable_concept_mapping.py`:

### Target Resources

The `TARGET_RESOURCES` list defines which FHIR resources to include. To add a new resource, simply append it to the list. All resource names must match the FHIR R4 spec exactly (PascalCase).

### Depth

`MAX_DEPTH = 4` — controls how deep to traverse nested fields. Depth is counted from the resource root:

- Depth 1: `Condition.code`
- Depth 2: `Condition.stage.summary`
- Depth 3: `Claim.item.detail.productOrService`
- Depth 4: `Claim.item.detail.subDetail.productOrService`

### Skipped Types

The script skips: `Extension`, `ModifierExtension`, `Identifier`, `Reference`, `Resource`, `ResourceContainer`, and the `meta` field. These either cause infinite recursion or don't carry clinical codes.

### Coded Types

`CODED_TYPES = {'CodeableConcept', 'Coding'}` — fields matching these types are collected. CodeableConcept contains Coding inside it, so when a CodeableConcept is found, the script stops recursing (doesn't duplicate the inner `.coding` field).

## Output Format

```typescript
export const SensitiveCodeableConceptFieldMapping: Record<
  string,
  { CodeableConcept?: string[]; Coding?: string[] }
> = {
  Condition: {
    CodeableConcept: [
      'bodySite',
      'category',
      'clinicalStatus',
      'code',
      'evidence.code',
      'severity',
      'stage.summary',
      'stage.type',
      'verificationStatus',
    ],
  },
  Encounter: {
    CodeableConcept: ['diagnosis.use', 'type' /* ... */],
    Coding: ['class', 'classHistory.class'],
  },
};
```

- **CodeableConcept fields**: contain `coding[]` array + `text` field
- **Coding fields**: standalone single codes (e.g., `Encounter.class`)
- Field paths are dot-separated relative to the resource (e.g., `dosageInstruction.route`)

## Reused Infrastructure

The script reuses patterns from `fhir-server/generatorScripts/generate_everything_operation_data.py`:

- `get_field_type_property()` — looks up a type's entity definition by name
- `handle_nested_fields()` — recursive traversal with skip patterns and cycle detection
- `primitive_types_dict` — from `FhirXmlSchemaParser.get_fhir_primitive_types()`
- `all_classes` — from `FhirXmlSchemaParser.generate_classes()`

## FHIR Binding Strength (Not in Output, but Useful Context)

Each CodeableConcept field has a binding strength in the FHIR spec:

- **required**: locked to a FHIR value set — CANNOT contain sensitive codes (e.g., `clinicalStatus`)
- **extensible**: should use value set, but CAN use other systems
- **preferred**: recommended codes, any system valid
- **example**: any code system — where sensitive codes primarily live (e.g., `Condition.code`)
- **unbound**: no binding defined — completely open

Only 6 fields across all 31 resources have `required` binding. The rest can potentially carry sensitive codes.

## Common Tasks

### Add a new FHIR resource

1. Add the resource name to `TARGET_RESOURCES` list (alphabetical order)
2. Run `make generate-codeable-concept-mapping`

### Update FHIR spec version

1. Copy updated XSD files from `fhir-server/generatorScripts/xsd/` to `generatorScripts/xsd/`
2. Copy updated `fhir_xml_schema_parser.py` from `fhir-server/generatorScripts/`
3. Run `make generate-codeable-concept-mapping`

### Change traversal depth

1. Edit `MAX_DEPTH` in the script
2. Run `make generate-codeable-concept-mapping`
