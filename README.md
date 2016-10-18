> Gloomy Dungeons 2 is also opensourced!<br />
> Check it out - https://github.com/restorer/gloomy-dungeons-2

# What is it?

It is old-school 3d shooter, indie game developed by small team.
It didn't use hi-end engine, so it looks more like wolf-3d than quake or even doom.
As side effect, it works on low-end phones pretty fast.
It has big sexy square pixels (*love*)... You should really love pixelart (like me :)

  - Code: restorer
  - Old-school graphics: www.nongnu.org/freedoom, www.lostgarden.com, restorer
  - Modern graphics: Denis Smoktunovich (smoktunovich@gmail.com)
  - Levels: zin (zin.wtf@gmail.com), restorer
  - Texts: zin (zin.wtf@gmail.com), restorer
  - Sound: www.nongnu.org/freedoom

This game is released under MIT License (http://www.opensource.org/licenses/mit-license.php).

# Product support

This product is already finished, so no long support is planned.

| Feature | Support status |
|---|---|
| New features | No |
| Non-critical bugfixes | No |
| Critical bugfixes | Yes, if it will be easy to understand where to fix |
| Pull requests | Accepted (after review) |
| Issues | Monitored, but if you want to change something - submit a pull request |
| Android version planned to support | Up to 8.x |
| Estimated end-of-life | Up to 2018 |

# Compiling

There are 2 version of game: normal version and hard oldschool version.
You can compile either by using build script or directly using gradle.

## Compile and install debug build

Normal version:

  - `./z-build fdroidnormal debug install` or
  - `./gradlew installFdroidnormalNormalWithoutanalyticsWithoutzeemoteWithoutrateofferDebug`

Hardcore version:

  - `./z-build fdroidhardcore debug install` or
  - `./gradlew installFdroidhardcoreHardcoreWithoutanalyticsWithoutzeemoteWithoutrateofferDebug`

## Compile release builds

To be able to compile release builds, create put your keystore file (or create new) to `tools/signing.keystore` and create `tools/signing.properties`:

```
keyAlias=put_key_alias_here
storePassword=put_keystore_password_here
keyPassword=put_key_password_here
```

Normal version:

  - `./z-build fdroidnormal release` or
  - `./gradlew assembleFdroidnormalNormalWithoutanalyticsWithoutzeemoteWithoutrateofferRelease`

Hardcore version:

  - `./z-build fdroidhardcore release` or
  - `./gradlew assembleFdroidhardcoreHardcoreWithoutanalyticsWithoutzeemoteWithoutrateofferRelease`

Search for result .apk files in build/outputs/apk/
