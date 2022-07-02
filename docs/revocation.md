<h1 align="center">
    Certificates revocation
</h1>

## Certificates validity (revocation) look up:

Some certificates might be revoked at some point of time.
To have related certificates state in the app up to date - separate API call is provide.
Opposite to the verifier app logic - calling only this single API endpoint - wallet application receives up to date information of certificates validity and updated UI respectively.
Related call is happening once per 24 hours and during certificate claiming.
In addition to this - it can be triggered manually via settings menu.