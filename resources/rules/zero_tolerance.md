# Rule: Zero Tolerance (Regulation Z § 1026.19(e)(3)(i))

## 1. Summary
The **Zero Tolerance Rule** is the strictest TRID compliance requirement. It mandates that the actual amount charged on the **Closing Disclosure (CD)** cannot exceed the estimated amount on the **Loan Estimate (LE)** by any margin (0% increase allowed). This ensures that lenders provide accurate estimates for costs within their direct control.

## 2. Legal Foundation
* **Regulation Z:** [12 CFR § 1026.19(e)(3)(i)](https://www.consumerfinance.gov/rules-policy/regulations/1026/19/#e-3-i)
* **Official Commentary:** [Comment for 19(e)(3)(i)](https://www.consumerfinance.gov/rules-policy/regulations/1026/interp-19/#19-e-3-i-Interp)
* **CFPB Guide:** [TILA-RESPA Integrated Disclosure Guide](https://www.consumerfinance.gov/compliance/compliance-resources/mortgage-resources/tila-respa-integrated-disclosures/) (Section 7: Good faith and tolerances)

## 3. Scope & Form Mapping
The rule applies to fees paid to the creditor, the mortgage broker, or their affiliates. In the standard TRID forms, these fees are primarily located in **Section A**.
| TRID Form Section | Legal Category | Item Examples |
| :--- | :--- | :--- |
| **Section A** | Origination Charges (Paid to Creditor/Broker) | Application Fee, Underwriting Fee, Points |
| **Section B** | Services Borrower Cannot Shop For (Unaffiliated) | Appraisal Fee, Credit Report Fee |
| **Section E** | Taxes and Other Government Fees | Transfer Taxes |

## 4. Engine Validation Logic
The `gritum.engine.rules.zero-tolerance` namespace should implement the following logic:
1.  **Selection**: Filter all items where `section == "A"`.
2.  **Semantic Match**: Link LE items to CD items based on description similarity.
3.  **Variance Calculation**:
    $$\text{Variance} = \text{Amount}_{CD} - \text{Amount}_{LE}$$
4.  **Determination**:
    * If $\text{Variance} \le 0$: **PASS**
    * If $\text{Variance} > 0$: **VIOLATION** (Flag for Cure)

## 5. Proof of Concept (Minimal Data Set)
Analysis of `le-a.pdf` and `cd-a.pdf`:
* **LE Entry**: Section A - "Application Fees" ($300.00)
* **CD Entry**: Section A - "App fees" ($320.00)
* **Matching**: Both represent the creditor's application fee.
* **Compliance Result**: **FAIL**. A $20.00 increase in a Zero Tolerance category (Section A) is a violation unless a valid "Changed Circumstance" is documented.

## 6. Cure Requirements
If a violation is detected, the creditor must:
1.  Refund the overcharged amount ($20.00 in the example).
2.  Provide a corrected CD reflecting the refund.
3.  Ensure the refund is issued within **60 calendar days** of consummation.

---
**Reference Link:** [CFPB Small Entity Compliance Guide (PDF)](https://files.consumerfinance.gov/f/documents/cfpb_tila-respa-integrated-disclosure_small-entity-compliance-guide.pdf)