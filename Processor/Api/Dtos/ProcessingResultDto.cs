namespace Api.Dtos;

public class ProcessingResponseDto
{
    public bool Success { get; set; }
    public string Message { get; set; }
    public byte[] ImageData { get; set; }
}