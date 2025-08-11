using Microsoft.AspNetCore.Mvc;
using Processing.Services;
using Api.Utils;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddScoped<Mapper>();
builder.Services.AddScoped<ProcessingService>();

var app = builder.Build();

app.MapControllers();

app.Run();