using System.Diagnostics;
using System.Text.Json;
using Processing.Models;
using SixLabors.ImageSharp;
using Processing.Services;

class Program
{
    static void Main()
    {
        /* Complete with your paths. */
        string testInputPath = @"C:\Users\augus\Desktop\TestInput.json";
        string testOutputPath = @"C:\Users\augus\Desktop\TestOutput.json";

        string jsonIn = File.ReadAllText(testInputPath);

        var options = new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        };

        ProcessingRequest request = JsonSerializer.Deserialize<ProcessingRequest>(jsonIn, options);
        ProcessingService service = new ProcessingService();

        ProcessingResult result = new ProcessingResult
        {
            Response = true,
            Message = "Testing",
            FileName = "ImageOut.jpg",
            ImageBase64 = null
        };

        service.Process(request, result);
        File.WriteAllText(testOutputPath, result.ImageBase64);
    }
}

