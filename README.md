<h1 align="center">
   EU Digital COVID Certificate Wallet App - Android
</h1>

<p align="center">
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/eu-digital-green-certificates/dgca-wallet-app-android?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-wallet-app-android?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#licensing">Licensing</a>
</p>

## About

This repository contains the source code of the EU Digital COVID Certificate Wallet App for Android.

The wallet app provides a user interface to store and manage personal DGCs directly on the phone. DGCs will be imported by scanning a base45-encoded QR code and decoding CBOR to JSON. Afterwards, it is symmetrically encrypted in the app’s sandbox and the symmetric key is stored in the system’s keychain. Multiple DGCs can be stored in the app. Access to the app is controlled via biometric data (e. g., Touch ID or Face ID). The wallet app can display any imported DGC as QR code for scanning and verifying with the verifier app.

**A note on using the apps and released APK files found in this GitHub organization**: The apps are reference implementations that cannot be used in production environments as-is, but rather need to be configured by EU member states to access their national backends. The released APK files are configured to work with the test environments and will not report correct results on "live" DCCs.

## Development

### Build

Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

In either case open a terminal pointing to the directory you put the sources in. The local build process is described afterwards depending on the way you choose.

#### XYZ (Maven, Docker ...) based build

- To build project - it's required to add config json file to application assets folder. Structure of the file should be similar to:
'app/src/acc/assets/wallet-context.jsonc' or 'app/src/tst/assets/wallet-context.jsonc', depending on chosen flavor.
After related file has been added - it's name should be passed via gradle properties:
gradlew -PCONFIG_FILE_NAME="config.json"

## Documentation  

- [App architecture](/docs/architecture.md)
- [DCC Revocation](/docs/revocation.md)
- [Verifiable credentials](/docs/verifiable_credentials.md)

## Support and feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **Issues**    | <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-wallet-app-android?style=flat"></a>  |
| **Other requests**    | <a href="mailto:opensource@telekom.de" title="Email DGC Team"><img src="https://img.shields.io/badge/email-DGC%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to contribute  

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors  

Our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Licensing

Copyright (C) 2021 T-Systems International GmbH and all other contributors

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
