# Specification: Credit-Based Billing Model

## 1. Executive Summary
This document defines the credit-based billing architecture for the Gritum TRID validation engine. The system uses an internal currency ("Credits") to decouple service pricing from currency fluctuations and to allow for flexible, granular pricing of LLM-intensive operations.

## 2. Currency Definition & Conversion
To ensure maximum pricing flexibility, we establish a fixed base conversion rate while allowing service costs to remain dynamic.

* **Base Conversion:** $0.01 USD = 10 Credits
    * *Note: Using a 1:10 ratio allows for integer-based pricing changes (e.g., increasing a 30-credit service to 35 credits) without requiring floating-point decimals.*
* **Parity:** The USD-to-Credit ratio remains constant to maintain trust and predictability for the customer.

## 3. Service Pricing Strategy
Instead of changing the price of Credits, we adjust the **Consumption Rate** per service.

| Service Type | Current Consumption | Strategy |
| :---         | :---                | :---     |
| **TRID Full Check** | 30 Credits ($0.03) | LLM parsing (LE/CD) + Matching + Rule Validation |
| **Section-only Check** | 10 Credits ($0.01) | Specific section validation (e.g., Section A only) |
| **Data Extraction** | 15 Credits | Raw JSON extraction without rule validation |

## 4. Operational Advantages
1.  **Price Agility:** If LLM API costs (e.g., OpenAI/Anthropic) increase, we simply update the `consumption-rate` in the database from 30 to 40 credits.
2.  **Pre-paid Cash Flow:** Customers purchase credit bundles upfront, providing immediate working capital.
3.  **Incentivized Volume:** We can offer "Bonus Credits" for larger USD deposits (e.g., "$50 gets you 50,000 + 5,000 bonus credits") without altering the core logic.

## 5. Technical Implementation (Clojure/Datomic)
The system must treat Credit balances as **immutable transaction logs** rather than a single mutable integer.

* **Atomic Transactions:** Every consumption must be an atomic operation.
    * `Check Balance` -> `Validate Sufficient Funds` -> `Deduct Credits` -> `Execute LLM Job`.
* **Ledger Table:**
    ```clojure
    {:id #uuid "..."
     :user-id "user-123"
     :amount -30
     :usage :trid-full-check
     :created-at #inst "2024-..." }
    ```

## 6. Future Considerations
* **Auto-Reload:** Optional feature for Enterprise clients to "top up" when balance falls below a specific threshold (e.g., 500 credits).

---
**Status:** Approved for implementation.
**Owner:** Antigravity Engineering Team