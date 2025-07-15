/* Regex Expression
 * {x, y}:
 *     A interval,
 *     min repeat 'x' times,
 *     ',' is sperator when no 'y' means at less x times,
 *     max repeat 'y' times,
 *     'y' can be ignore
 * (statement):
 *     '()' as a whole, can connect with other statement.
 */
const regex = /pattern/gi;

/* Usage: test
 * Test the string is match the regex expression.
 */
const regex_test = function () {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const testEmail = "example@example.com";

    if (emailPattern.test(testEmail)) {
        console.log("This email pass the validation.");
    } else {
        console.log("This email failed of the validation.");
    }
}

/* Usage: matches
 * Get a list about all matched part of string.
 */
const regex_matcher = function () {
    const testMatchText = "Lifemark is a walid life recoder."
    const textMatchesPattern = /life/g; // g: Means global search all
    const matches = testMatchText
        .toLocaleLowerCase()
        .match(textMatchesPattern);

    if (matches) {
        console.log(`Find matched all ${matches.length}: `, matches);
    } else {
        console.log("Nothing found");
    }
}

/* Usage: search
 * Find out the first matched place in the string.
 */
const regex_search = function () {
    const searchString = "Hello world";
    const firstWord = searchString.search(/world/);

    console.log(`\"World\" start with index ${firstWord}`);
}

/* Usage: replace
 * Replace the part of string matches.
 */
const regex_replace = function () {
    const replacementString = "The quick brown fox jumps over the lazy dog";
    const replacedString = replacementString.replace(/quick/, "slow"); // Replace the first value of ""quick"
    const replacedAllWithString = replacementString.replace(/the/gi, "a") // i Means igonre upper and lower case, g Means global replace
    console.log(replacedString)
    console.log(replacedAllWithString)
}

// UUID: 7bcb1f7d-f1bb-4cda-a0cf-e86ef1686474
const regex_uuid = function (uuid) {
    const regex = /^[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}$/;
    return regex.test(uuid);
}

function generateUUID() {
    let d = new Date().getTime();
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

const uuid = generateUUID();
console.debug(regex_uuid(uuid));