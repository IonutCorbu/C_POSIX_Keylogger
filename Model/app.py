from flask import Flask, request, jsonify
import joblib
import numpy as np

app = Flask(__name__)

MODEL_PATH = "output/password_token_model.joblib"
THRESHOLD = 0.6

model = joblib.load(MODEL_PATH)

def read_words_from_txt(file_storage):
    content = file_storage.read().decode("utf-8", errors="replace")
    words = [line.strip() for line in content.splitlines()]
    words = [w for w in words if w]  
    return words

@app.post("/predict")
def predict():
    if "file" not in request.files:
        return jsonify({"error": "Missing file field. Use multipart/form-data with field name 'file'."}), 400

    f = request.files["file"]
    if not f.filename:
        return jsonify({"error": "Empty filename."}), 400

    words = read_words_from_txt(f)
    if not words:
        return jsonify({"error": "No words found in file."}), 400

    # print(words)
    X = np.array(words, dtype=str)

    try:
        proba = model.predict_proba(X)[:, 1]
    except Exception as e:
        print(f"Prediction error: {e}")
        return jsonify({"error": "Prediction failed.", "details": str(e)}), 500

    results = []
    for w, p in zip(words, proba):
        print(f"{w} - {p}")
        if float(p) > THRESHOLD:
            results.append({"word": w, "prob_password": float(p)})

    results.sort(key=lambda x: x["prob_password"], reverse=True)

    return jsonify({
        "threshold": THRESHOLD,
        "total_words": len(words),
        "password_candidates": len(results),
        "results": results
    })

@app.get("/health")
def health():
    return jsonify({"status": "ok"})

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)