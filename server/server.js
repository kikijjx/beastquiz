const express = require('express');
const compression = require('compression');
const fs = require('fs');

const app = express();
const port = 5000;

app.use(compression());

function readCsv(file_path) {
    try {
        const data = fs.readFileSync(file_path, 'utf-8');
        const rows = data.split('\n');
        const header = rows[0].split(',');

        const jsonData = rows.slice(1).map(row => {
            const values = row.split(',');
            
            if (values.length === header.length) {
                const entry = {};
                header.forEach((key, index) => {
                    entry[key] = values[index];
                });
                return entry;
            }
            
            return null;
        }).filter(entry => entry !== null);

        return jsonData;
    } catch (error) {
        console.error(`Ошибка чтения CSV: ${error.message}`);
        return [];
    }
}

const csvFilePath = 'animals.csv';
const data = readCsv(csvFilePath);

app.get('/get_data', (req, res) => {
    res.json(data);
});

app.listen(port, () => {
    console.log(`Сервер запущен: http://localhost:${port}`);
});
