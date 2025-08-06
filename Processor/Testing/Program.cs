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
        string testInputPath = @"C:\Users\...\TestInput.json";
        string testOutputPath = @"C:\Users\...\TestOutput.json";

        string jsonIn = File.ReadAllText(testInputPath);

        var options = new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        };

        ProcessingRequest request = JsonSerializer.Deserialize<ProcessingRequest>(jsonIn, options);
        ProcessingService service = new ProcessingService();

        ProcessingResult result = service.Process(request);
        File.WriteAllText(testOutputPath, result.ImageBase64);
    }
}

