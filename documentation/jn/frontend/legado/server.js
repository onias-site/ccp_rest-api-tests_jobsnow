const express = require('express');
const path = require('path');
const app = express();

app.use(express.static(__dirname));
app.use(express.static(path.join(__dirname, 'dist')));
app.get('/*', function (req, res) {
    res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

// Listen to the App Engine-specified port, or 2200 otherwise
const PORT = process.env.PORT || 2200;
app.listen(PORT, () => {
    console.log(`Server listening on port ${PORT}...`);
});