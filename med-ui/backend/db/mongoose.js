const mongoose = require('mongoose')
require('dotenv').config()


mongoose.connect(process.env.DATABASE, {
    useNewUrlParser: true,
    useCreateIndex: true,
    useFindAndModify: true,
    useUnifiedTopology: true,
    dbName: 'hospital',

}).then(() => {
    console.log("Db connected")
})