# java-financing-system

![CI](https://github.com/lopesdev07/java-financing-system/actions/workflows/ci.yml/badge.svg)

# Java Financing System | v2.0 - 2026-08-15

**Description:** Financing application developed in Java for practice on backend development. Implements a login/register system, alongside different types of financings (real estate and vehicle) that can be simulated, saved, edited, and canceled by their owner, using PRICE and SAC amortization methods. All data is persisted in MySQL via JDBC.

## Technical Highlights & Challenges

While writing the test suite, a real IDOR (Insecure Direct Object Reference) vulnerability was found in the repository layer: `findById` was assigning the requesting user's ID to the returned object instead of the actual owner's ID from the database, making ownership checks in `findFinancingById` and `updateFinancing` pass regardless of who actually owned the financing. This was fixed by reading the real `user_id` from the database and adding it directly to the query's `WHERE` clause.

The SAC amortization calculation had a subtler bug: the installment field only ever held the value from the loop's last iteration, silently reporting the smallest (last) installment as if it were "the" installment, when SAC installments actually decrease month over month. This was resolved by exposing the first installment, last installment, and the constant monthly decrement, so any installment can be derived mathematically without re-running the loop.

Financing cancellation went through several design iterations: the decision of which status to assign was originally made by the view layer and was moved into the service layer, which now owns both the transition rule (only `APPROVED` financings can be canceled) and the resulting immutability guarantee — once a financing is `CANCELED`, further edits are blocked, preserving it as an honest historical record instead of a soft-deleted-but-still-editable state.

The financing-creation flow in the console menus had a control-flow bug: creation methods caught their own exceptions internally without signaling failure, so the menu proceeded to display and offer to save a financing even when creation had actually failed. Fixed by making creation methods return a success flag, so the rest of the flow only proceeds when creation genuinely succeeded.

## Utilized Technologies
- Java 17
- Maven
- MySQL + JDBC
- JUnit 5 + Mockito (unit testing)
- SLF4J + Logback (logging)
- GitHub Actions (CI)
- IDE: IntelliJ

---

## Project Structure
**java-financing-system/**

**database** → schema.sql

**java** → exceptions, model, repository, service, util, view, Main.java

**test** → unit tests mirroring the main package structure (model, service, util)

---

## Testing

This project has **116 unit tests** (JUnit 5 + Mockito), covering:
- Amortization calculations (PRICE and SAC) for both financing types
- Password hashing and authentication logic
- Menu input validation loops
- Conditional `toString()` display logic per property/vehicle type
- Full service-layer business rules: input validation, ownership checks, financing cancellation and editing rules — with the repository layer mocked, so no test touches a real database

Run all tests locally with:
```bash
mvn test
```

Tests also run automatically on every push and pull request via GitHub Actions (see the badge above).

---

## Logging

The application uses SLF4J + Logback for structured logging, separate from the console messages shown to the user. Log levels follow this convention:
- `INFO` — successful business actions (login, financing simulated/saved/canceled/updated)
- `WARN` — expected validation failures (invalid CPF, wrong password, invalid financing data)
- `ERROR` — unexpected failures (database errors)

Logs are written to `logs/application.log` (rotated daily, 7-day retention) and are not shown in the console, which is reserved for user-facing messages.

---

## Data Bank

This project utilizes **MySQL** for data persistence.

Database structure is defined in `database/schema.sql`.

### How to set up the Database
1. Create a new database in MySQL with the name you prefer (e.g., `financing_project`)
2. Execute the following command in your terminal, replacing `your_user`, `database_name` and `schema.sql` with your MySQL username, the name of the database you created and the path to the schema.sql file, respectively:
```bash
mysql -u your_user -p database_name < database/schema.sql
```

## How to execute the project
*-- Important --*
**To avoid versioning sensitive information, environment variables are used.**

Before running the project, it's **required** to configure the following environment variables in your system:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

*Restarting your IDE or terminal is required for the changes to take effect.*

**Windows (PowerShell/CMD):**
setx DB_URL "jdbc:mysql://localhost:3306/financing_project"
setx DB_USER "root"
setx DB_PASSWORD "your_password"
**Linux / macOS:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/financing_project"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

1. **Clone this repository**
```bash
   git clone https://github.com/lopesdev07/java-financing-system
```
2. **Open the project**
   Open the project in your IDE (e.g., IntelliJ)
3. **Compile and run**
   Locate `Main.java` in the IDE and hit **Run**

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Next Steps

- Add Javadocs (evaluating whether this is worth doing for this project)

**Other:**
- Total migration of the project to Spring Boot
