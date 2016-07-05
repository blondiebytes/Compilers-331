program DuplicateVariable (input, output);
var a : integer;
    a: array [2..4] of integer;
begin
    a := 5;   { This never gets executed in this phase }
end.
