# 🌈 PetBacked World - Magical Pet Care & Community 🐾

Welcome to **PetBacked World**! A magical, Disney-style pet care portal where rainbow-colored wonders and cute companions make pet management and neighborhood socialization a fairy-tale experience.

---

## 🎨 Designed Character Homepage Preview

Here is a sneak peek of our beautiful, animated, and interactive Disney-style homepage!

![PetBacked World Homepage Mockup](petbacked_homepage_mockup.jpg)

---

## 🚀 Key Interactive Web Features
1. **Interactive Mascot Pippo 🐶**: Built with inline SVG. Pippo's sparkling eyes follow your mouse movements. Click him for random cute dialogs and a burst of rainbow-colored sparkle stars!
2. **Rainbow Sparkle Cursor Trail 💫**: Moving your mouse cursor creates a sparkling trail of rainbow stars that gently fade out.
3. **Floating Disney Clouds ☁️**: Header with floating cartoon-like fluffy clouds.
4. **Interactive Cards 🩺🐾💬**: Learn about our weather-based Walk Mate, AI health advisors, and Chat Rooms. Clicking card buttons generates a confetti star explosion.
5. **Interactive Guestbook ✏️**: Leave a message for the pets and trigger interactive star explosions.

---

## 🔧 Resolved Issues & Walkthrough

We successfully resolved all compilation, bean creation, validation, database query (JPQL/HQL), and configuration errors across the codebase. All 58 unit tests now pass.

### 1. Spring Bean & Dependency Injection Fixes
- **`AdvertisementService`**: Registered it as a Spring `@Service` Bean to resolve context loading issues.
- **`WalkRecordController`**: Removed unused repository and controller constructor dependencies that caused `@WebMvcTest` slice tests to fail context initialization.
- **CORS Config**: Fixed key spelling typo `${app.cors.allowed-orgins}` to `${app.cors.allowed-origins}` and corrected method spelling `PATCT` to `PATCH`.

### 2. JPA Query (JPQL/HQL) Syntax & Type Mapping Fixes
- **`Advertisement`**: Renamed misspelled field `proirty` to `priority`.
- **`AdvertisementRepository`**: Corrected typos (`Advertisment` -> `Advertisement`, `stractDate` -> `startDate`, and assignment operator `>- : now` to `>= :now`).
- **`ChatRoomRepository`**: Fixed query grammar (missing parameter colon `:keyword`, spelling of `coalesce` and `lower`, and fixed sort attribute `created` to `createdAt`).
- **`ShortsCommentRepository`**: Fixed column mapping error (`c.shortsId` -> `c.shortId`).
- **`ShortsLikeRepository`**: Corrected numeric alias `1` to `sl` in JPQL query.

### 3. Spring Data JPA Method Name & Signature Fixes
- **`ChatMessageRepository`**: Fixed property resolution errors (`ByRoom` -> `ByRoomId`, `AndLessThan` -> `AndIdLessThan`).
- **`MemberRepository`**: Renamed query signature method `findByProviderAndDeletedAtIsNull` to `findByProviderAndProviderIdAndDeletedAtIsNull` to resolve parameter-count mismatch.

### 4. Controller Path Mapping & Request Validation Fixes
- **`ChatController`**: Fixed path patterns (missing closing braces `{roomId}`, path variable names mismatch `{roomsId}` -> `{roomId}`, `{roomid}` -> `{roomId}`, and malformed syntax `{member/role` -> `{memberId}/role`).
- **`WalkWeatherController`**: Updated `@DecimalMax` annotation for `lat` parameter from `33.0` to `43.0` to permit valid latitude coordinates inside South Korea.

### 5. Weather Calculation & General Config Fixes
- **`KmaGridConverter`**: Corrected LCC projection factor `sf` calculation by adding missing offset factor `Math.PI * 0.25`.
- **`KmaClient`**: Corrected parameter ordering for `KmaWeatherSnapshot` constructor call (`airTemp, humidity, windSpeed`).

### 6. Test Suite Assertion Fixes
- **`AiSearchControllerTest`**: Fixed missing dot in JSON path (`$data` -> `$.data`).
- **`KmaBaseTimeTest`**: Changed method call to `forUltraSrtFcst(now)` for forecast time test.
- **`WalkWeatherControllerTest`**: Fixed JSON path (`data` -> `$.data`), corrected request URL, and fixed parameter name mismatch.
- **`WalkRecordControllerTest`**: Fixed `DateTimeParseException` by formatting instant parse parameters with seconds.

---

## 📈 Verification Status

We executed the full Gradle test task:
```powershell
$env:JAVA_HOME=""; .\gradlew.bat test --no-daemon
```

### Result:
```text
BUILD SUCCESSFUL in 23s
4 actionable tasks: 2 executed, 2 up-to-date
```
All **58 unit tests** are successfully compiled, verified, and passing!
