program testingScope (input, output);
	var a, b, c: integer;

	procedure one (b, d : integer);
		var a : integer;
		begin
			write(b);       { Print param b, which is actually var a }
			b := b + 1;     { Add one to b param, adding one to a by reference }
			a := 2 * b + d; { Using b as param not as var b }
			write(a);       { Print value of local var a NOT global var a }
			c := a          { Changing the value of global var c }
		end
	begin
		a := 1;
		b := 3;
		one(a,b); { Passes proc one a & b which treats as b & d respectively }
		write(a);
		write(b);
		write(c)
	end
.