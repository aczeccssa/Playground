const https = require("https");
const fs = require("fs");
const path = require("path");

function downloadFile(url, outputPath) {
    return new Promise((resolve, reject) => {
        const file = fs.createWriteStream(outputPath);

        https.get(url, (response) => {
            // Check HTTP status code
            if (response.statusCode !== 200) {
                reject(new Error(`Download failed, status: ${response.statusCode}`));
                return;
            }

            // Stream write to file
            response.pipe(file);

            // Watch file write complete
            file.on("finish", () => {
                file.close();
                resolve(outputPath);
            });
        }).on("error", (err) => {
            // Delete the created file when an error is made
            fs.unlink(outputPath, () => {
            });
            reject(err);
        });
    });
}

try {
    const url = "https://example.com/file/download/file_name";
    const outputDir = "./downloads";
    const fileName = path.basename(new URL(url).pathname);
    const outputPath = path.join(outputDir, fileName);

    // Makesure output dir exisit
    if (!fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, {recursive: true});
    }

    console.log(`Start download: ${url}`);
    await downloadFile(url, outputPath);
    console.log(`Download complete: ${outputPath}`);
} catch (err) {
    console.error("Failed when downloading:", err.message);
}