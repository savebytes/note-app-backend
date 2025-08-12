# SPS — Merchant ID Verification, Signup & Signup & Login Flow (Updated)

**Last updated:** August 12, 2025

A concise, technical + non‑technical explanation of the new merchant verification, signup and login flow for the SPS (Sports Parking System) app so the product/management team and QA can understand and approve it.

---

# 1. Overview

When the SPS app launches on a Clover device the first step is to verify whether the device's `merchant_id` is already registered in the backend. Based on the backend response the app either: (A) proceeds to the login screen (merchant exists), or (B) shows a signup prompt linking the user into a web signup flow (merchant missing). After successful signup the user returns to the app and the merchant id can be validated again — then the user is taken to the login screen.

Key points:

* Single entry point: **Merchant ID Verification Screen** on app open.
* API used: **POST [https://sportsparkingsystem.com/api/check-merchant](https://sportsparkingsystem.com/api/check-merchant)** (sends `merchant_id`).
* Backend responses determine app navigation and user messaging.
* Once a merchant account is created or verified a dedicated subdomain is created for that merchant (e.g. `https://ankit.sportsparkingsystem.com/api/`).

---

# 2. Components / Actors

* **Clover device** running the SPS Android app
* **SPS App** (Android/Jetpack Compose) — Merchant ID screen, WebView, Login screen
* **Backend API** — `/api/check-merchant` and supporting signup services
* **Signup Web Portal** — hosted webpage where merchants register and select plans
* **Database & DNS/TLS provisioning** — to create merchant subdomains

---

# 3. Endpoint: check-merchant

**URL:** `POST https://sportsparkingsystem.com/api/check-merchant`

**Request (body)**

* `merchant_id` — required (fetched from device)

**Recommended behavior / response model**

* Always return `HTTP 200` with a JSON payload that contains an `exists` boolean and additional fields. This simplifies client handling compared to returning different status codes for success/failure.

**Success (merchant exists) — example**

```json
HTTP/1.1 200 OK
{
    "status": 1,
    "code": 200,
    "message": "Merchant ID exists.",
    "data": [
        {
            "id": 1,
            "fname": "Chiagg",
            "lname": "Ghevariya",
            "photo": "1633437779slider-logo.png",
            "username": "chiragggg",
            "email": "gcb1196@gmail.com",
            "number": "8756453456",
            "city": "Bhavnagar",
            "state": "Gujarat",
            "address": "HI",
            "country": "India",
            "merchant_id": "4004041",
            "domain": "chirag",
            "billing_fname": "B Name",
            "billing_lname": "B L Name",
            "billing_photo": null,
            "billing_email": null,
            "billing_number": "8756548767",
            "billing_city": "Bhavnar B",
            "billing_state": "Gujarat B",
            "billing_address": "Adress B La Name",
            "billing_country": "India B",
            "shpping_fname": "Chirag",
            "shpping_lname": "Ghevariya",
            "shpping_photo": null,
            "shpping_email": null,
            "shpping_number": "9867564534",
            "shpping_city": "Bhavnagar",
            "shpping_state": "Gujarat",
            "shpping_address": "shpping_number  Address",
            "shpping_country": "India",
            "created_at": "2021-06-11 11:41:58",
            "updated_at": "2021-10-05 07:12:59",
            "status": 1,
            "verification_link": null,
            "email_verified": "yes",
            "fb_id": null,
            "credit": 10000,
            "online": "0",
            "company_name": null
        }
    ]
}

```

**Not found (merchant does not exist) — example**

```json
HTTP/1.1 200 OK
{
    "status": 0,
    "code": 404,
    "message": "Merchant ID not found.",
    "link": "https://sportsparkingsystem.com/register",
    "data": []
}

```

(If your backend currently uses a different format or status codes, the client code should be updated to the above model or QA should agree on the exact contract.)

---

# 4. User flow (step-by-step)

1. **App open** → **Merchant ID Verification Screen** is displayed.

   * UI: spinner/progress and message "Verifying device merchant id..."
   * App reads stored `merchant_id` from device (or fetches from Clover platform API) and fires `POST /api/check-merchant`.

2. **API response handling**

   * **If `exists: true`**

     * Save minimal `merchant_data` locally (encrypted preferences).
     * Redirect user to **Login Screen** (standard username/password or token flow). The login screen should show merchant name and subdomain if available.
   * **If `exists: false`**

     * Show a modal dialog with the `message` from the API and a prominent button such as **"Sign up"**. Also show an explanatory line: "No account found for this device — register now."
     * Dialog should include the `signup_url` (do not display raw URL if you prefer just the button).

3. **User taps Sign up**

   * Open a **WebView Activity** and load the provided `signup_url`.
   * The web signup flow allows the merchant to register, choose a plan and complete payment if required.
   * At the end of the web signup flow the website should show a clear success message: e.g. **"Registration complete — please open the SPS app on your Clover device."**

4. **User returns to the app** (manually or via OS back button)

   * On resuming the Merchant ID Verification Screen the app re-runs `POST /api/check-merchant`.
   * If the backend has completed provisioning, this request will now return `exists: true` and the app proceeds to the **Login Screen**.

5. **Subdomain creation**

   * When a merchant is created (via signup or backend provisioning) a unique subdomain is created (pattern: `https://{subdomain}.sportsparkingsystem.com/api/`).
   * The `subdomain` and the `api_base` should be returned in the `merchant_data` so the app (or other services) can use merchant-scoped endpoints.

---

# 5. UI / UX details (suggested copy & behavior)

* **Merchant ID Verification Screen**

  * Title: "Verifying merchant"
  * Body: "Checking this device for a registered merchant..."
  * Actions: none while checking; network error shows Retry.

* **Signup Dialog (when merchant missing)**

  * Title: "Merchant not registered"
  * Body: API `message` (e.g. "merchant\_id not found. Register your business to continue.")
  * Primary button: **"Sign up"** → opens WebView
  * Secondary: **"Cancel"** (returns to a safe idle screen or exits)

* **WebView**

  * Title: "SPS Signup"
  * Should allow navigation, but prevent external credential leakage.
  * After successful registration the web page should display the exact instruction: "Open the SPS app on your Clover device to complete verification," to avoid confusion.

* **Login Screen**

  * Show merchant name and subdomain to reassure the user.

---

# 6. Edge cases & recommended handling

* **Network failures**: show inline error with Retry. Use exponential backoff for automated retries.
* **Propagation delay for subdomain / provisioning**: backend may take a few seconds to provision DNS/SSL. Show the user a friendly message and keep retrying `check-merchant` on app resume or when user taps Retry. Consider polling with small backoff for \~60–120s.
* **Partial signup**: if signup portal returns "pending", show message: "Registration in progress — please try again in a few minutes." Do not allow login.
* **Multiple devices with same merchant\_id**: clarify whether merchant\_id is unique per merchant or per device. If unique per merchant, multiple devices should validate to same merchant account.
* **Invalid/malformed merchant\_id**: validate locally before sending — show an error and do not call API.

---

# 7. Security & privacy considerations

* Always use HTTPS for API and signup pages.
* Do not include sensitive tokens or API keys in WebView URL query strings.
* Store any merchant data on the device encrypted (e.g., Android Keystore / EncryptedSharedPreferences).
* Validate server TLS certificate; consider certificate pinning for production.
* Sanitize and validate `merchant_id` server-side to prevent injection attacks.
* Rate-limit the `check-merchant` endpoint to avoid abuse.

---

# 8. Backend & DevOps notes (what the backend team must ensure)

* **Immediate response format**: return `exists` boolean with `merchant_data` or `signup_url` + `message`.
* **Subdomain generation**: create subdomain when merchant account is created and return it in the response. Ensure DNS and TLS issuance (e.g., via Let's Encrypt or an automated CA) are part of the provisioning pipeline.
* **Provisioning lag**: if DNS/TLS issuance is asynchronous, backend should return a `status` or `provisioning` flag so the app can show an appropriate message while waiting.
* **Signup link**: `signup_url` must be mobile-friendly (responsive) for WebView. Include `source=clover` or similar query param so analytics and UX can be tailored.

---

# 9. Acceptance criteria / QA checklist

* [ ] On app start, `POST /api/check-merchant` is called with device `merchant_id`.
* [ ] If merchant exists, app navigates to Login screen and displays merchant name.
* [ ] If merchant missing, dialog shows message and Sign up button opens the provided `signup_url` in WebView.
* [ ] After completing signup on web and reopening the app, `check-merchant` returns `exists:true` and app navigates to Login.
* [ ] Subdomain is returned in `merchant_data` and points to a working API base URL.
* [ ] Proper error handling for network errors and provisioning delays.

---

# 10. Example curl (for devs / testers)

**Merchant exists (test)**

```bash
curl -X POST "https://sportsparkingsystem.com/api/check-merchant" \
  -H "Content-Type: application/json" \
  -d '{"merchant_id":"abc123"}'
```

**Expected response**: `exists:true` with `merchant_data` (see example above).

---

# 11. Questions / decisions for product owner

* Preferred dialog copy & button label for the signup prompt?
* Subdomain naming policy (use `merchant_name` slug vs `merchant_id`)?
* Expected max provisioning time for DNS/TLS (so UI can show timeouts and messaging).
* Should the WebView be able to deep-link back into the app on completion (e.g. via custom URL scheme)? If yes, the signup page must support returning to `sps://complete` or similar.

---

If you want, I can: provide a sequence diagram (ASCII or PNG), produce a short one-page summary slide for your boss, or convert this doc to a PDF/printable layout. Tell me which format you prefer.
