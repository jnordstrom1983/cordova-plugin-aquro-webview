var exec = require('cordova/exec');

exports.CreateView = function(name, url, top, left, width, height, success, error) {
    exec(success, error, "AquroWebView", "CreateView", [name,url,top,left,width,height]);
};

exports.MoveView = function(name, top, left, width, height, success, error) {
    exec(success, error, "AquroWebView", "MoveView", [name,top,left,width,height]);
};

exports.ShowView = function(name, success, error) {
    exec(success, error, "AquroWebView", "ShowView", [name]);
};
exports.HideView = function(name, success, error) {
    exec(success, error, "AquroWebView", "HideView", [name]);
};

exports.DeleteView = function(name, success, error) {
    exec(success, error, "AquroWebView", "DeleteView", [name]);
};

exports.RemoveAllViews = function(success, error) {
    exec(success, error, "AquroWebView", "RemoveAllViews", []);    
};

exports.SetURL = function(name,url, success, error) {
    exec(success, error, "AquroWebView", "SetURL", [name, url]);
};
