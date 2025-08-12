cd C:/Users/stone/Documents/Docs/Projects/Cognyte/Insiders/backend/

scp -i C:/Users/stone/Documents/Docs/Backups/Insiders_23_07_2025/Insiders2.pem target/backend-1.0-SNAPSHOT.jar ubuntu@ec2-3-74-161-90.eu-central-1.compute.amazonaws.com
echo 'Jar uploaded to EC2 !'

echo 'Connecting to EC2 !'
ssh -i C:/Users/stone/Documents/Docs/Backups/Insiders_23_07_2025/Insiders2.pem ubuntu@ec2-3-74-161-90.eu-central-1.compute.amazonaws.com
