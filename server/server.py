from flask import Flask, jsonify
import csv

app = Flask(__name__)

def read_csv(file_path):
    data = []
    with open(file_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            data.append(row)
    return data

csv_file_path = 'animals.csv'
data = read_csv(csv_file_path)

@app.route('/get_data', methods=['GET'])
def get_data():
    return jsonify(data)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
