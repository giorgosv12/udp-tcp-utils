# Communicating with a Server using UDP and TCP protocols
***

This projects contains some methods for communicating with a remote server
through TCP and UDP protocols to send/ receive information. The methods that 
are included are:

- `echo`: This method sends an echo signal to a specified port using UDP protocol.
- `image`: This method generates an image using specified parameters and saves it to a file on the local machine. The image is created by sending UDP packets to a specified port.
- `soundDPCM`: This method generates a sound signal using differential pulse-code modulation (DPCM) and saves it to a file on the local machine.
- `soundAQDPCM`: This method generates a sound signal using adaptive quantization differential pulse-code modulation (AQ-DPCM) and saves it to a file on the local machine.
- `copterTelemetryUDP`: This method simulates a telemetry feed from a remote control helicopter by sending UDP packets to a specified port.
- `vehicleOBDII`: This method simulates data output from a vehicle's on-board diagnostics (OBD-II) system and sends the data to a specified port using TCP protocol.
- `video`: This method generates a video using specified parameters and saves it to a file on the local machine. The video is created by converting a series of images to a video format using FFmpeg.
- `copterTCPtest`: This method tests the TCP connection to a remote control helicopter by sending a test message to a specified port.

This project was created as a part of _Networks II_ course. The description of the project is also provided (Greek).