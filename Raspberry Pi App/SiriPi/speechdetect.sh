#!/bin/bash
sudo rm voice.flac 
# FLAC encoded example
ffmpeg -i $1 voice.flac
curl \
  --data-binary @voice.flac \
  --header 'Content-type: audio/x-flac; rate=8000' \
  'https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&pfilter=0&lang=en-US&maxresults=6'
