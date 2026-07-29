---
name: generate-body-system
description: Scaffold a new body system scoring module with score calculator, aggregation, data retriever, composition builder, assessment orchestrator, and tests. Use when creating a new health scoring domain (e.g., metabolic, neurological, endocrine).
argument-hint: <system-name> <scoring-spec>
allowed-tools: [Read, Write, Edit, Bash, Glob, Grep]
---

# Generate Body System Scorer

You are scaffolding a new body system scoring module for the AI Health Optimization platform.

## Arguments

The user provided: $ARGUMENTS

Parse the arguments to determine:
1. **System name** — the body system identifier (e.g., `metabolic`, `endocrine`, `neurological`)
2. **Scoring specification** — either inline description of metrics/weights/scoring criteria, or a reference to where the spec can be found

If the scoring specification is missing or unclear, ask the user for:
- What metrics should be scored (with weights summing to 1.0)
- The scoring criteria for each metric (what values map to what point ranges)
- Any age/gender-specific reference ranges
- The grade scale interpretation (what score ranges mean clinically)

---

## Architecture Overview

Every body system module follows a strict hexagonal architecture pattern with these collaborators:

```
DataRetriever (FHIR I/O) → Aggregation (pure logic) → ScoreCalculator (pure logic) → CompositionBuilder (FHIR output)
                                                    ↓
                                        Assessment (orchestrator, extends BodySystemAssessment)
```

All modules live under `aihealthoptimization/body_systems/<system_name>/` with corresponding tests in `tests/body_systems/<system_name>/`.

---

## Files to Generate

For a system named `<system_name>`, create these files:

### 1. `aihealthoptimization/body_systems/<system_name>/__init__.py`

Package init that exports the main public API classes.

### 2. `aihealthoptimization/body_systems/<system_name>/score_calculator.py`

The core scoring engine. Pattern to follow:

```python
"""
<System Display Name> Score Calculator

Calculates <system> health scores from patient data.
Each component scorer returns a score (0-100), category, and explanation.
The overall score is a weighted combination of components.

Weighting:
    - <Metric1>: <weight1>%
    - <Metric2>: <weight2>%
    ...
"""

from __future__ import annotations
from datetime import date
from statistics import median
from typing import Any

from aihealthoptimization.common.scoring import (
    STANDARD_GRADE_SCALE,
    STANDARD_GRADE_THRESHOLDS as GRADE_THRESHOLDS,
    WeightedScoreCalculator,
    score_to_grade,
)

# Weight constants
WEIGHT_<METRIC1> = 0.XX
WEIGHT_<METRIC2> = 0.XX
# ... (must sum to 1.0)

class <SystemName>ScoreCalculator:
    """Calculate <system> health scores from patient data."""

    # Static methods for each component scorer
    @staticmethod
    def score_<metric>(value, age=40, gender="unknown") -> dict[str, Any]:
        """Score <metric> on a 0-100 scale. Returns {score, category, explanation, value}."""
        if value is None:
            return {"score": None, "category": "no_data", "explanation": "...", "value": None}
        # Scoring logic here...

    # calculate_score method (required by ScoreCalculatorProtocol)
    def calculate_score(self, patient_data: dict[str, Any]) -> dict[str, Any]:
        """Calculate overall score. Returns {overall_score, grade, component_scores, ...}."""

    # calculate_daily_scores method (required by ScoreCalculatorProtocol)
    def calculate_daily_scores(self, daily_metrics, patient_data) -> list[dict[str, Any]]:
        """Calculate per-day scores. Returns sorted list of {date, overall_score, grade, component_scores}."""

    @staticmethod
    def summarize_daily_scores(daily_scores) -> dict[str, Any]:
        """Returns {median_score, median_grade, days_scored}."""
```

**Critical rules for score calculators:**
- Every component scorer is a `@staticmethod` returning `dict[str, Any]` with keys: `score`, `category`, `explanation`, `value`
- `score` is `float | None` (None when no data)
- Use `WeightedScoreCalculator` for the overall score — it handles missing data renormalization
- `calculate_score` and `calculate_daily_scores` are required by the `ScoreCalculatorProtocol`
- Include `_assess_data_quality` and `_generate_recommendations` helper methods
- Use the standard grade scale (A≥90, B≥80, C≥70, D≥60, F<60) unless the spec says otherwise

### 3. `aihealthoptimization/body_systems/<system_name>/aggregation.py`

Three-stage aggregation pipeline for FHIR Observations.

```python
"""
<System> Observation Aggregation

Three-stage pipeline:
  Stage 1 – Group observations by day
  Stage 2 – Aggregate within each day (metric-specific strategy)
  Stage 3 – Aggregate across days (median for robustness)
"""

from aihealthoptimization.common.aggregation import (
    AggregationStrategy,
    AggregationWindow,
    MetricAggregationSpec,
    aggregate_across_days,
    aggregate_day,
    aggregate_metrics_from_grouped_days,
    collapse_metric_by_device,
    group_observations_by_day,
)
from aihealthoptimization.common.constants import LOINC_CODES, is_valid_value
from aihealthoptimization.common.fhir.retrieval import (
    get_observation_code,
    get_observation_date,
    get_observation_device,
    get_observation_value,
    get_loinc_or_custom_code,  # Use this if any codes are custom (non-LOINC format)
)
```

**Key aggregation patterns:**
- Look up LOINC codes from `LOINC_CODES` dict using the metric's `name` key from devicecodex
- Use `get_loinc_or_custom_code` if any observation codes are custom (not matching `^\d{4,6}-\d$` pattern)
- Use `get_observation_code` if all codes are standard LOINC
- Provide `empty_<system>_metrics()` factory function
- Provide `aggregate_<system>_observations()` for period-level aggregation
- Provide `aggregate_<system>_observations_by_day()` for daily aggregation (used by daily scorer)
- Aggregation strategies: MIN for "resting" values, MEAN for composition metrics, MEDIAN for vital signs

### 4. `aihealthoptimization/body_systems/<system_name>/data_retriever.py`

FHIR data retrieval. Pattern:

```python
from aihealthoptimization.common.fhir.retrieval import (
    BasePatientDataRetriever,
    FHIRClientProtocol,
    build_code_token,
    format_date_for_fhir,
)

class Patient<SystemName>DataRetriever(BasePatientDataRetriever):
    def get_complete_data(self, patient_id, as_of_date=None) -> dict[str, Any]:
        """Returns {patient, <system>_metrics, raw_observations}."""
```

**Key rules:**
- Subclass `BasePatientDataRetriever`
- Use `build_code_token(code)` for each observation code to handle LOINC vs custom codes
- Return a dict with `patient`, `<system>_metrics`, and `raw_observations` keys
- The `patient` key comes from `self.get_patient_profile(patient_id)`

### 5. `aihealthoptimization/body_systems/<system_name>/composition_builder.py`

FHIR Composition output:

```python
from aihealthoptimization.common.fhir.composition_builder import (
    BodySystemCompositionBuilder,
    BodySystemConfig,
    ComponentMeta,
)

_CONFIG = BodySystemConfig(
    body_system_id="<system-name>",  # kebab-case
    display_title="<System Display Name>",
    code_system="https://www.icanbwell.com/<system-name>-health-score",
    components=[
        ComponentMeta("key", "code-slug", "Display Name", loinc_code="...", display_unit="..."),
        # One per component in the scorer
    ],
)

class <SystemName>CompositionBuilder:
    _builder = BodySystemCompositionBuilder(_CONFIG)

    @classmethod
    def build(cls, *, patient_id, score_result, ...) -> Composition:
        return cls._builder.build(...)
```

### 6. `aihealthoptimization/body_systems/<system_name>/assessment.py`

Orchestrator (thin subclass):

```python
from aihealthoptimization.common.assessment import BodySystemAssessment, BodySystemAssessmentConfig

_CONFIG = BodySystemAssessmentConfig(
    system_name="<system_name>",
    component_keys=["metric1", "metric2", ...],  # Must match component_scores keys
)

class <SystemName>HealthAssessment(BodySystemAssessment):
    def __init__(self, fhir_client, *, write_to_fhir=True, lookback_days=30):
        super().__init__(
            retriever=...,
            calculator=...,
            aggregation_func=aggregate_<system>_observations_by_day,
            composition_builder=...,
            writer=CompositionFHIRWriter(fhir_client) if write_to_fhir else None,
            config=_CONFIG,
        )
```

### 7. `tests/body_systems/<system_name>/__init__.py`

Empty file.

### 8. `tests/body_systems/<system_name>/test_score_calculator.py`

Tests for all component scorers:

```python
import pytest
from aihealthoptimization.body_systems.<system_name>.score_calculator import <SystemName>ScoreCalculator

class Test<Metric1>Scoring:
    def test_no_data_returns_none_score(self): ...

    @pytest.mark.parametrize("value,expected_category", [...])
    def test_scoring_tiers(self, value, expected_category): ...

class TestOverallScore:
    def test_all_optimal_scores_high(self): ...
    def test_all_poor_scores_low(self): ...
    def test_missing_components_still_scores(self): ...
    def test_no_data_returns_none_score(self): ...
    def test_weighted_formula_correct(self): ...
```

**Test requirements:**
- Test every tier/category for each component scorer
- Use `@pytest.mark.parametrize` for data-driven cases
- Test `None` input for every scorer
- Test the overall weighted formula produces the expected value
- Test that missing components get renormalized correctly

### 9. `tests/body_systems/<system_name>/test_aggregation.py`

Tests for the aggregation pipeline:

```python
class TestAggregate<System>Observations:
    def test_empty_observations(self): ...
    def test_single_day_metrics(self): ...
    def test_multi_day_aggregation(self): ...

class TestAggregateByDay:
    def test_groups_by_day(self): ...
    def test_averages_same_day(self): ...
```

---

## LOINC Code Discovery

Before generating code, check which LOINC codes are available in devicecodex for the metrics needed. Look at:
- `devicecodex/registry/definitions.py` for metric definitions (name, code, category, valid_range)
- `aihealthoptimization/common/constants/__init__.py` for imported code groupings
- The `LOINC_CODES` dict maps `metric_name` → `code_string`

If a metric doesn't have a standard LOINC code, it may use a custom code (like `body-water-pct`). Custom codes won't have `http://loinc.org` system URI in observations. Use `get_loinc_or_custom_code` in aggregation when this applies.

---

## Validation Steps

After generating all files:

1. Run `python3 -c "import ast; ast.parse(open('<file>').read())"` on each file to verify syntax
2. Run `docker rm -f aihealthoptimization_test 2>/dev/null; docker compose run --rm --name aihealthoptimization_test dev pytest tests/body_systems/<system_name>/ -v` to run tests
3. If tests fail, fix the issues and re-run
4. Run the full test suite to check for regressions: `docker rm -f aihealthoptimization_test 2>/dev/null; docker compose run --rm --name aihealthoptimization_test dev pytest tests/ --tb=short`

---

## Common Patterns and Gotchas

1. **Grade scales**: Use `STANDARD_GRADE_SCALE` (90/80/70/60) unless the spec provides a custom interpretation. Import from `aihealthoptimization.common.scoring`.

2. **Missing data handling**: The `WeightedScoreCalculator` renormalizes weights when components are missing. Never penalize patients for missing data — let the calculator redistribute weight.

3. **Age/gender lookup tables**: When scoring criteria vary by demographics, use a list of tuples `[(age_max, threshold1, threshold2, ...)]` pattern with a `_get_<metric>_bracket()` helper function.

4. **Component score dict format**: Every scorer MUST return `{"score": float|None, "category": str, "explanation": str, "value": <raw_value>}`.

5. **Data retriever return shape**: The `get_complete_data` method must return a dict where the calculator can find its metrics. Convention is `{"patient": {...}, "<system>_metrics": {...}, "raw_observations": [...]}`.

6. **Aggregation code extraction**: Use `get_loinc_or_custom_code` (accepts system-less codes) vs `get_observation_code` (LOINC only). Check your codes against the `_LOINC_CODE_RE = re.compile(r"^\d{4,6}-\d$")` pattern to decide.

7. **Weight trend / derived metrics**: If a metric requires comparing values over time (trend) or combining multiple observations (e.g., muscle_mass_kg / body_weight → percentage), handle this in the aggregation layer, not the scorer.

8. **Test observations**: Use this factory pattern in tests:
```python
def _make_observation(code, value, date, device="Device/test-1"):
    coding = {"code": code}
    coding["system"] = "http://loinc.org" if re.match(r"^\d{4,6}-\d$", code) else ""
    return {
        "resourceType": "Observation",
        "id": f"obs-{code}-{date}-{value}",
        "code": {"coding": [coding]},
        "effectiveDateTime": f"{date}T08:00:00Z",
        "valueQuantity": {"value": value, "unit": "unit"},
        "device": {"reference": device},
    }
```

---

## Reference Files

Read these files to understand the exact patterns:
- `aihealthoptimization/body_systems/cardiovascular/score_calculator.py` — canonical score calculator
- `aihealthoptimization/body_systems/musculoskeletal/score_calculator.py` — musculoskeletal example
- `aihealthoptimization/body_systems/musculoskeletal/aggregation.py` — aggregation with derived metrics
- `aihealthoptimization/common/scoring.py` — WeightedScoreCalculator, GradeScale, TrendResult
- `aihealthoptimization/common/assessment.py` — BodySystemAssessment base class
- `aihealthoptimization/common/aggregation.py` — shared aggregation utilities
- `aihealthoptimization/common/fhir/retrieval.py` — BasePatientDataRetriever, observation helpers
- `aihealthoptimization/common/fhir/composition_builder.py` — BodySystemConfig, ComponentMeta
- `tests/body_systems/musculoskeletal/test_score_calculator.py` — test patterns