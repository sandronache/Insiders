using Shared.Models;

namespace Processing.Interfaces;

public interface IProcessingService
{
    public ProcessingResult Process(ProcessingRequest request);
}