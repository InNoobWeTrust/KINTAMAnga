Define 4 environment variables in build and add keyfile to designated path

- `$KEYFILE`: Path to signing key, should be absolute path
- `$STORE_PASSWORD`: Keystore password
- `$KEY_ALIAS`: alias, as the name stated (default "androidkey")
- `$KEY_PASSWORD`: password for key

Then in deployment for release APK, execute this command:

```shell
gradlew :app:assembleRelease \
    -Pandroid.injected.signing.store.file=$KEYFILE \
    -Pandroid.injected.signing.store.password=$STORE_PASSWORD \
    -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
    -Pandroid.injected.signing.key.password=$KEY_PASSWORD
```

Or to build bundle

```shell
gradlew :app:bundleRelease \
    -Pandroid.injected.signing.store.file=$KEYFILE \
    -Pandroid.injected.signing.store.password=$STORE_PASSWORD \
    -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
    -Pandroid.injected.signing.key.password=$KEY_PASSWORD
```
