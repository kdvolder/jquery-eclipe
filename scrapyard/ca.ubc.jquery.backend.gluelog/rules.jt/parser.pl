% TODO: this is sort of hackish... requires some code in JTransformerQuery as well.
getVariables('',[]).
getVariables(Term,Vars) :- 
	string_to_atom(Term,X),atom_to_term(X,_,Vars).

parseFileExtension(F,E) :- concat_atom([_|Tl],'.',F),listTail(Tl,T),string_to_atom(T,E).
parseFileName(F,N) :- concat_atom(L,'/',F),listTail(L,T),string_to_atom(T,N).

listTail(I,[],I).
listTail(_,[L|Lr],T) :- listTail(L,Lr,T).
listTail([L|Lr],T) :- listTail(L,Lr,T).