namespace Processing.Operations;
public class ImageOperationFactory
{
    private readonly Dictionary<OperationType, IImageOperation> _operations;

    public ImageOperationFactory()
    {
        _operations = new Dictionary<OperationType, IImageOperation>()
        {
            { OperationType.GrayscaleFilter, new GrayscaleFilter() },
            { OperationType.InvertFilter, new InvertFilter() }
        };
    }

    public IImageOperation? GetOperation(OperationType name)
    {
        return _operations.TryGetValue(name, out IImageOperation operation) ? operation : null;
    }
}