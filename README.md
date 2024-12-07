This service will basically take the files present in the s3 bucket and then convert it to zipped file and upload to same location

Sample Curl:-
curl --location 'http://localhost:8080/api/v1/zipFilesAndUpload' \
--header 'Content-Type: application/json' \
--data '{
    "bucketName" : "uj-bucket-28",
    "outputZipKey" : "zippedfiles.zip",
    "fileKeys" : "fileOne.txt,fileTwo.txt,fileThree.txt,fileFour.pdf,Timesheet.xlsx"
}'

Response:-
Zipped and uploaded successfully!
