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

# BR-A05 — Customer Login ID Generation

**Status:** Approved by Nitin Kumar.

## 1. ID Format
- Customer Login ID must contain exactly 7 digits.
- First 5 digits are derived from the normalized customer name.
- Last 2 digits are taken from the mobile number.
- Return the ID as a String to preserve leading zeros.

## 2. Name Normalization
- Reject null or blank names.
- Convert uppercase English letters to lowercase.
- Retain only English letters (a-z).
- Remove spaces, digits, punctuation and special characters.
- Reject names containing no English letters after normalization.

## 3. Name Hash Algorithm

Use a polynomial rolling hash with base 31.

hash = 0

For each character:
    hash = (31 * hash + characterPosition) % 100000

Character positions:
a = 1, b = 2, ... z = 26

The resulting hash is the initial prefix.

## 4. Mobile Number
- Follow BR-A03: exactly 10 digits.
- First digit must be 6, 7, 8 or 9.
- Use the last two digits as the ID suffix.

## 5. Collision Handling
- Check the generated ID using the supplied Predicate<String>.
- If the ID exists, increment the prefix by one.
- Wrap from 99999 to 00000.
- Try at most 100000 prefixes.
- If all IDs are occupied, throw IdGenerationException.

## 6. Worked Examples

| Name | Normalized | Prefix | Mobile | Login ID |
|---|---|---|---|---|
| Al | al | 00043 | 9876543210 | 0004310 |
| Daksh Arya | daksharya | 94268 | 9876543245 | 9426845 |
| Sam Lee | samlee | 76365 | 9876543282 | 7636582 |
| Riya Sharma | riyasharma | 75033 | 9876543267 | 7503367 |
| Al (collision) | al | 00044 | 9876543210 | 0004410 |

## 7. Implementation Helpers
- nameHash(String name)
- nextPrefix(int prefix)
- buildId(int prefix, String mobile)

These helpers will be package-visible static methods for unit testing.
