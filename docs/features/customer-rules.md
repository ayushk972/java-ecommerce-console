# Customer Registration & Login ID Rules
7-digit Customer Login ID algorithm (BR-A05), validation rules (BR-A02..A04), and PBKDF2 hashing notes.


# Customer Registration Rules

## Story
S1-08 — Customer model and CustomerValidator

## Business Rules

### BR-A02 — Customer Name
- Minimum length: 2 characters.
- Maximum length: 50 characters.
- Allowed characters: English letters, spaces, hyphens,
  apostrophes and dots.
- At least two letters are required.
- Digits and other special characters are not allowed.
- Leading and trailing whitespace is removed.
- Consecutive whitespace is collapsed into one space.

### BR-A03 — Mobile Number
- Exactly 10 digits.
- First digit must be 6, 7, 8 or 9.
- Letters and whitespace are rejected.
- Mobile uniqueness is enforced by the CustomerRepository
  and registration service, not by CustomerValidator.

### BR-A04 — Password
- Minimum length: 8 characters.
- Maximum length: 64 characters.
- Must not be blank.
- Customer stores only the password hash.
- Password or password hash must never appear in:
  - console output
  - application logs
  - audit records
  - Customer.toString()

## Customer Model

Customer is immutable.

Identity:
- Internal identity: UUID
- Customer Login ID: String containing exactly 7 digits

Leading zeros in the Login ID are preserved.

Equality and hash code:
- Based only on UUID.

Mobile masking:
- Displays six asterisks followed by the last four digits.
- Example: 9876543210 -> ******3210

## Validation Behaviour

CustomerValidator.validateRegistration() validates name,
mobile and password in a single operation.

When multiple fields are invalid, all errors are returned
through one ValidationException.

CustomerValidator.normalizeName() trims leading/trailing
whitespace and collapses internal whitespace.

## Examples

| Input | Result |
|---|---|
| Daksh Arya | Valid name |
| A. | Invalid: fewer than 2 letters |
| Daksh123 | Invalid: digits |
| Mary-Jane | Valid name |
| O'Neil | Valid name |
| 9876543210 | Valid mobile |
| 5876543210 | Invalid mobile |
| password123 | Valid password |
| 1234567 | Invalid password |

## Responsibility Boundaries

CustomerValidator:
- Validates registration field formats.
- Does not check duplicate mobile numbers.
- Does not hash passwords.
- Does not generate Customer Login IDs.
- Does not persist customers.

CustomerRegistrationServiceImpl (S1-12):
- Orchestrates registration.
- Normalizes the name.
- Checks duplicate mobile.
- Generates Customer Login ID.
- Hashes password.
- Creates and saves Customer.
- Writes audit record after successful save.
