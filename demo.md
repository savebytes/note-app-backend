# SPS — Merchant Verification, Signup & Login Flow

**Purpose:** Concise documentation of the updated SPS app flow for merchant verification, signup, and login.

---

## Flow Diagram (Mermaid)

```mermaid
flowchart TD
  Start([Open SPS app])
  Verify[Merchant ID verification screen\nFetch merchant_id from device]
  API[/POST https://sportsparkingsystem.com/api/check-merchant\nBody: merchant_id/]
  Exists{merchant_id exists?}
  Login[Redirect to Login screen(merchant data received)]
  Dialog[Show dialog: merchant not found\nMessage + signup link + button]
  Webview[Open WebView → signup page\nUser registers & selects plan]
  Complete[Signup complete → show "Open SPS app on Clover device" message]
  Reopen[Re-open app → Merchant ID verification]
  Subdomain[Create merchant subdomain\nhttps://{merchant}.sportsparkingsystem.com/api/]
  End([User on Login / App ready])

  Start --> Verify --> API --> Exists
  Exists -- Yes --> Login --> Subdomain --> End
  Exists -- No --> Dialog --> Webview --> Complete --> Reopen --> API
```

---

## Step-by-step

1. **App launch** → Merchant ID Verification screen fetches `merchant_id` from the device and calls `POST https://sportsparkingsystem.com/api/check-merchant`.
2. **If merchant exists** → API returns merchant data → Redirect to Login.
3. **If merchant does not exist** → API returns a signup link and message → Show dialog with button.
4. **User taps button** → Open signup page in WebView → User registers and selects plan.
5. **After signup** → Website prompts user to re-open SPS app on Clover device.
6. **Re-opening app** → Merchant ID verification runs again → Merchant now exists → Redirect to Login.
7. **Subdomain creation** → Upon successful verification or registration, create merchant-specific subdomain (e.g., `https://ankit.sportsparkingsystem.com/api/`).

---

## Conclusion

The SPS app first checks if a merchant ID exists on the server. If found, the user goes straight to login. If not, the user is guided to register through an in-app signup page. Once registration is complete, the merchant ID is recognized, and the user can log in. Each verified or registered merchant gets their own dedicated subdomain for API access.

