# 🌈 PetBacked World - Cute Mascot Pet Care & Community 🐾

Welcome to **PetBacked World**! A magical pet care portal redesigned with adorable themes inspired by **Shin-chan (짱구/흰둥이)**, **Doraemon (도라에몽)**, and **Catch! Teenieping (하츄핑)**. Experience interactive mascot animations, dynamic color themes, cute sound effects, and a magical item-drawing game.

---

## 🎨 Designed Character Homepage Preview

Here is a preview of our redesigned, animated, and interactive themed homepage!

![PetBacked World Homepage Mockup](petbacked_homepage_mockup.jpg)

---

## ✨ Key Cute & Interactive Features (새로운 테마 및 기능 설명)

### 1. 🐶 Multi-Mascot Character & Theme Navigator (캐릭터 및 테마 전환)
Clicking the circular navigator bubbles at the top changes the active mascot and dynamically transforms the entire website's design to match their personality:
*   **🐶 삐뽀 (Pippo)**: The friendly guard dog of PetBacked. Switches to the colorful **Rainbow Theme** with floating stars and bubbles.
*   **☁️ 흰둥이 (Shiro)**: The fluffy marshmallow dog from Shin-chan. Switches to the warm **Yellow Theme** with floating bones, clouds, and soccer balls.
*   **🐱 도라에몽 (Doraemon)**: The robotic blue cat helper. Switches to the classic **Sky Blue Theme** with floating bells, bamboo copters, and dorayaki pancakes.
*   **👑 하츄핑 (Hachuping)**: The lovable love fairy Teenieping. Switches to the sweet **Pastel Pink Theme** with floating hearts, crowns, and ribbons.

### 2. 👁️ Interactive SVG Eyes Tracking (마우스 커서 눈동자 추적)
Each mascot is drawn using custom vector inline SVG. Their eyes dynamically track your mouse movements across the screen to create a lively, engaging experience. Clicking on a character triggers customized speech bubbles in their signature tone (e.g., "~츄!", "~앙!", "대나무 헬리콥터~!").

### 3. 🎹 8-Bit Web Audio Sound Synthesizer (복고풍 8비트 사운드 이펙트)
To bring a retro game-like charm, clicking buttons, switching mascots, or drawing items synthesizes cute chime arpeggios, jump beeps, and magical sweeps dynamically using the browser's Web Audio API. *No external audio file downloads are required!*

### 4. 🎁 Doraemon's Magic Pocket Drawer (마법의 주머니 선물 뽑기)
Clicking on Doraemon's 4D pocket triggers a spin animation and extracts a random magical hybrid pet tool or snack card:
*   🚁 **대나무 헬리콥터 목줄** (Bamboo Copter Leash) - High-flying walks!
*   🍪 **초코비 개껌** (Chocobi Dog Chew) - Milk chew inside a chocobi box.
*   💖 **하츄 하트 목걸이** (Hachu Heart Collar) - Makes pets dance Hachuping dance.
*   🥞 **도라야끼 맛 츄르** (Dorayaki Flavor Churu) - Sweet red bean cat lick.
*   🛡️ **액션가면 댕댕 망토** (Action Mask Cape) - Become the hero of the alley.
*   🚪 **어디로든 문 이동장** (Anywhere Door Carrier) - Instant teleport to the vet or park.

### 5. ✏️ Character-Themed Guestbook (캐릭터 맞춤형 방명록)
Writing messages in the guestbook automatically appends custom character ending suffixes matching the active theme:
*   *Shiro Theme* -> Appends **"~앙!"** 🐶
*   *Doraemon Theme* -> Appends **"~에몽!"** 💙
*   *Hachuping Theme* -> Appends **"~츄!"** 💖

---

## 🔧 Resolved Backend & Codebase Issues (이전 해결된 백엔드 버그)

We successfully resolved all compilation, bean creation, validation, database query (JPQL/HQL), and configuration errors across the codebase. All 62 unit tests pass.

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
- **`Role` & `Member`**: Fixed typo `Role.MEBER` to `Role.MEMBER` across class definitions, constructors, and tests.

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
- **`JwtTokenProviderTest`**: Fixed algorithm assertion typos (`aig` -> `alg`, `HSS12` -> `HS512`) and added length validations.

---

## 📈 Verification Status (테스트 검증 상태)

We executed the full Gradle test task:
```powershell
$env:JAVA_HOME=""; .\gradlew.bat test --no-daemon
```

### Result:
```text
BUILD SUCCESSFUL in 26s
4 actionable tasks: 2 executed, 2 up-to-date
```
All unit tests are successfully compiled, verified, and passing!
