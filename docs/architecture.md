<h1 align="center">
    Wallet App Architecture
</h1>

The wallet app provides functionality to import digital green certificates (DCC) and Verifiable credentials (VC/SHC) alongside with other types of certificates.
It's possible to import .jpeg or .pdf files. If during import system is able to recognise known certificate - it's stored as data. In other case it's stored directly as file.
Imported certificates can be verified over structure correctness, validity and rules of destination/departure countries.
To validate certificate (verify it's structure, expiration time) app verifies the signature with the keys provided by the wallet app’s backend.

![App architecture overview](/docs/resources/application_architecture.png)

### Modules

Application consists of set of module.
External modules are used for certificates verification:
- [decoder] (https://github.com/eu-digital-green-certificates/dgca-app-core-android) - certificates structure and validity verification.
- [engine](https://github.com/eu-digital-green-certificates/dgc-certlogic-android) - verification of certificates compliance with rules.
In-app modules are representing general application logic like:
- [app] - certificate cards management. Importing of certificates. Displaying and verifying certificates data.
and branching different certificate type-specific logic:
- [dcc] (Digital green certificate)
- [vc] (Verifiable credentials)
- [shc] (Smart health card)
- [divoc / icao] (Sample modules, to demonstrate ability for extension).



### General application flow

Interaction with the app starts with an authorization, after passing it, entry point is certificates screen: <img src="/docs/resources/main-screen-empty.png" width="200" />
Here you can import certificates: <img src="/docs/resources/main-screen-empty-import.png" width="200" />.
The most typical flow to import certificate is via scanning QR code: <img src="/docs/resources/scanner-screen.png" width="200" />.
After scanning certificates you see it's details: <img src="/docs/resources/scanner-screen.png" width="200" /> where you can claim it by pressing `Save` button.
To claim certificate - you need to enter it's TAN: <img src="/docs/resources/scanned-certificate-enter-tan.png" width="200" /> and pressing `Next`.
After claiming certificate, you can see it in the list: <img src="/docs/resources/main-screen-not-empty.png" width="200" />.
By pressing on certificate card in the list you can see it's details: <img src="/docs/resources/claimed-certificate-details.png" width="200" />, <img src="/docs/resources/claimed-certificate-details-functions.png" width="200" />.
From here you can navigate to verification screen by pressing `Check Validity`: <img src="/docs/resources/claimed-certificate-check-validity.png" width="200" />.
Validating certificate you can receive different results: <img src="/docs/resources/claimed-certificate-valid.png" width="200" />, <img src="/docs/resources/claimed-certificate-invalid.png" width="200" />.
