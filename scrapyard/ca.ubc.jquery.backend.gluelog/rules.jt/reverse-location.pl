% -------------------------------------------------
%
% Context Stuff
%
% -------------------------------------------------
reverseLocation('context',E,File,Offset,Length) :-
	locationJQ(E,File,Offset,Length).

reverseLocation('context',E,File,Offset,Length) :-
	locationJQ(X,File,Offset,Length),typeJQ(X),package(E,X).
	
% -------------------------------------------------
%
% Elements stuff
%	
% -------------------------------------------------	
getLocationInformation(X,File,Offset,Length) :-
	sl_argT(X,_,O,L),
	sum(Offset,Length,OE),(Offset > O),sum(O,L,E),(E > OE),
	sourceLocation(X,File,_,_).
	
reverseLocation('element',E,File,Offset,Length) :-
 	getLocationInformation(X,File,Offset,Length),accesses(_,E,X).
 	
reverseLocation('element',E,File,Offset,Length) :-
 	getLocationInformation(X,File,Offset,Length),calls(_,E,X).
 	
