program ArrayDeclTest (input, output);
var x : array [1..5] of real;
    y : array [15..100] of integer;
begin
    x[1] := 6.783   { This never gets executed in this phase }
end.
