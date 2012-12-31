<?php
// Where the file is going to be placed 
$target_path = "uploads/";
$myFile = "uploads/voice.3gp";
unlink($myFile);
$myFile = "voice.flac";
unlink($myFile);
/* Add the original filename to our target path.  
Result is "uploads/filename.extension" */
$target_path = $target_path . basename( $_FILES['uploadedfile']['name']); 

if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
    //echo "The file ".  basename( $_FILES['uploadedfile']['name']). 
    " has been uploaded";
   chmod ("uploads/".basename( $_FILES['uploadedfile']['name']), 0644);
} else{
    //echo "There was an error uploading the file, please try again!";
   //echo "filename: " .  basename( $_FILES['uploadedfile']['name']);
   //echo "target_path: " .$target_path;
}
exec("./speechdetect.sh uploads/voice.3gp > speech.results");
$results = file_get_contents("speech.results");
$results = explode("\"utterance\":\"",$results);
$results = $results[1];
$results = explode("\",\"",$results);
$results = $results[0];
$results = strtolower($results);
$myFile = "testFile.txt";
$fh = fopen($myFile, 'w') or die("can't open file");
$stringData = $results;
fwrite($fh, $stringData);
fclose($fh);
$results = trim($results);
if ($results == "radio on"){
echo "Starting FOX FM";
exec("sudo mplayer http://sc.fox.com.au");
}
if ($results == "music off"){
echo "Turning Music Off";
exec("sudo pkill mplayer");
}

if ($results == "shut up"){
echo "Music Muting";
exec("sudo amixer set --card=0 PCM 1000%- > /dev/null");
}

if ($results == "shut down"){
echo "Shutting Down";
exec("sudo halt");
}


if ($results == "reboot"){
echo "Rebooting";
exec("sudo reboot");
}

if ($results == "music quiet"){
echo "Turning Down Volume";
exec("sudo amixer set --card=0 PCM 80% > /dev/null");
}
if ($results == "full volume"){
echo "Turning Volume upto the Max";
exec("sudo amixer set --card=0 PCM 1000%+ > /dev/null");
}

if (explode(" ",$results)[0] == "youtube"){
$myFile = "youtube.mp3";
unlink($myFile);
$videoname = str_replace("youtube","",$results);
$videoname = trim($videoname);
$videoname = str_replace(" ","+",$videoname);
echo "Downloading " . $videoname . " From Youtube"; 
$linkresults = file_get_contents("http://gdata.youtube.com/feeds/api/videos?alt=rss&vq=" . $videoname . "&max-results=1&orderby=relevance");
$linkresults = explode("v=",$linkresults);
$linkresults = $linkresults[1];
$linkresults = explode("&amp",$linkresults);
$linkresults = "http://www.youtube.com/watch?v=" . $linkresults[0];
$myFile = "testFile.txt";
$fh = fopen($myFile, 'w') or die("can't open file");
$stringData = $linkresults;
fwrite($fh, $stringData);
fclose($fh);
exec("sudo ./youtube-dl -o youtube.mp3 -f 18 " . $linkresults);
exec("sudo mplayer youtube.mp3");
}
?>

