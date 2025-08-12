using Microsoft.AspNetCore.Mvc;
using SixLabors.ImageSharp;
using Processing.Services;
using Api.Utils;
using Api.Dtos;

namespace Api.Controllers;

[ApiController]
[Route("api/")]
public class ProcessingController : ControllerBase
{
   private readonly Mapper _mapper;
   private readonly ProcessingService _service;

   public ProcessingController(Mapper mapper, ProcessingService service)
   {
       _mapper = mapper;
       _service = service;
   }

   [HttpPost]
   public async Task<IActionResult> Process([FromForm] ProcessingRequestDto request)
   {
       var requestModel = await _mapper.ToRequestModel(request);
       var result = _service.Process(requestModel);

       using var stream = new MemoryStream();
       await result.Image.SaveAsJpegAsync(stream);

       return File(stream.ToArray(), "image/jpeg");
   }
}