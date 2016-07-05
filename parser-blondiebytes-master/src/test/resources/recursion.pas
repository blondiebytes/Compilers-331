Program recursionTest (input,output);
Var
   x,y : integer;

function gcd (a, b: integer) : result integer;
begin
   if b mod a <> 0 then
       gcd := gcd(b, a mod b)
   else
      gcd := b
end
begin
   read (x,y);
   write(x,y);
   if x>y then write (gcd(x, y))
end.