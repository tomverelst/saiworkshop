var express = require('express');
var http = require('http');
var EC = require('elliptic').ec;
var router = express.Router();
var atob = require('atob');
var btoa = require('btoa');

var amqp = require('amqplib/callback_api');

var Wallet = function(address, name, publicKey, privateKey, balance){
    this.address = address;
    this.name = name;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
    this.balance = balance;
};

var Transaction = function (sender, recipient, funds) {
    this.sender = sender;
    this.recipient = recipient;
    this.funds = funds;
};

var BlockchainService = function(host, port){
    this.host = host;
    this.port = port;
};

BlockchainService.prototype.getWallets = function(callback){
    return http.get({
        host: this.host,
        port: this.port,
        path: '/wallets'
    }, function(response) {
        var body = '';
        response.on('data', function(d) {
            body += d;
        });
        response.on('end', function() {
            var result = JSON.parse(body);
            var wallets = [];
            for(var i = 0; i < result.wallets.length; i++){
                var w = result.wallets[i];
                wallets[i] = new Wallet(
                    w.address,
                    w.name,
                    w.publicKey,
                    w.privateKey,
                    w.balance);
            }
            callback(wallets);
        });
    });
};

BlockchainService.prototype.getWallet = function(id, callback){
    return http.get({
        host: this.host,
        port: this.port,
        path: '/wallets/'+id
    }, function(response) {
        if (response.statusCode === 200) {
            var body = '';
            response.on('data', function(d) {
                body += d;
            });
            response.on('end', function() {
                    var w = JSON.parse(body);
                    var wallet = new Wallet(
                        w.address,
                        w.name,
                        w.publicKey,
                        w.privateKey,
                        w.balance);
                    callback(wallet);
            });
        }
    });
};

var ec = new EC('secp256k1');

Transaction.prototype.generateSignature = function () {
    console.log("Generating signature...");
    var binaryKey = atob(this.sender.privateKey);
    var key = ec.keyFromPrivate(binaryKey);

    var dataString = this.sender.publicKey + this.recipient.publicKey + parseFloat(this.funds);
    var msgHash = [];
    var buffer = new Buffer(dataString, 'utf16le');
    for (var i = 0; i < buffer.length; i++) {
        msgHash.push(buffer[i]);
    }

// Sign the message's hash (input must be an array, or a hex-string)
    var signature = key.sign(msgHash);

// Export DER encoded signature in Array
    var derSign = signature.toDER();
    if(key.verify(msgHash, derSign)){
        this.signature = btoa(derSign);
        console.log("Generated signature: " + this.signature);
    } else {
        console.log("Signature could not be generated");
    }
};

BlockchainService.prototype.createTransaction = function(sender, recipient, funds, callback){
    console.log("Sending " + funds + " coins from " + sender + " to " + recipient);
    bc.getWallet(sender, function(senderWallet){
        bc.getWallet(recipient, function(recipientWallet){
            var tx = new Transaction(senderWallet, recipientWallet, funds);
            tx.generateSignature();
            sendTransaction(tx);
        });
    });
};

var channel = null;
var queue = 'transactions.group1';

var rabbitUrl = process.env.RABBIT_URL || 'amqp://localhost:5672';

console.log("Connecting to " + rabbitUrl);

amqp.connect(rabbitUrl, function(err, conn) {
    if(err){
        console.log(err);
        return;
    } else {
        console.log("Connected to " + rabbitUrl);
        conn.createChannel(function(err, ch) {
            channel = ch;
            ch.assertQueue(queue);
        });
    }
});

function sendTransaction(tx){
    channel.sendToQueue(queue, Buffer.from(JSON.stringify({
        "sender": tx.sender.address,
        "recipient": tx.recipient.address,
        "funds": parseFloat(tx.funds),
        "signature": tx.signature
    })));
}

var fakeCoinHost = process.env.FAKECOIN_HOST || 'localhost';
var fakeCoinPort = process.env.FAKECOIN_PORT || 8080;

var bc = new BlockchainService(fakeCoinHost, fakeCoinPort);

router.get('/', function(req, res){
    console.log("Getting wallets....")
    bc.getWallets(function(wallets){
        return res.json( { "wallets": wallets} );
    });
});

/* Create transaction */
router.post('/sign', function(req, res) {
    bc.createTransaction(req.body.sender, req.body.recipient, req.body.funds);

    res.json({"transaction":"todo"});
});

module.exports = router;
