let localVideo = document.getElementById("local")
let remoteVideo = document.getElementById("remote")


var stream_func = function(stream){
    remoteVideo.srcObject = stream
    remoteVideo.className = "host"
    localVideo.className = "client"
}

let peer
function init(userId) {
    peer = new Peer(userId, {
        host: "192.168.1.15",
        port: 9000,
        path: "/videocallapp"
    })
    peer.on("open",function(conn){
        //we will make a call to a kotlin in android.
    })
    peer.on("call", function(call){
        navigator.getUserMedia({video: true,audio: true}, function(stream){
            localVideo.srcObject = stream
            localStream = stream
            //peerjs library will automaticaly send the stream 
            call.answer(stream)
            call.on("stream", stream_func)
        },function(err) {
            console.log('Failed to get local stream' ,err)
        })
    })
}


//clientId-> who we  want to call
function startCall(clientId) {
    navigator.getUserMedia({audio: true,video: true}, function(stream){
        localVideo.srcObject = stream
        localStream = stream
        const call = peer.call(clientId, stream)
        call.on('stream', stream_func)
    },function(err) {
        console.log('Failed to get local stream' ,err)
    })
}
//b is a boolean if True we will play the video, othervise we will stop the video. But because of Kotlin we created b as a String which work like boolean
function changeVideo(result) {
    var bool_result = (result == "true");
    localStream.getAudioTracks()[0].enabled = bool_result //play or stop the video
} 


// same idea like we did in the toggleVideo but this time we will play or stop the AUDIO
function changeAudio(result) {
    var bool_result = (result == "true");
    localStream.getAudioTracks()[0].enabled = bool_result //play or stop the audio
} 

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

var change_opacity = function(element){
    element.style.opacity = 1 
}

localVideo.onplaying = change_opacity(localVideo)
remoteVideo.onplaying = change_opacity(remoteVideo)

