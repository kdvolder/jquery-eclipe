adornmentFlags(X,Num) :- findall(N,adornmentFlag(X,N),Nums),sumlist(Nums,Num).

adornmentFlag(X,Y) :- enumConstantT(X,_,_,_,_),Y=10.

adornmentFlag(X,1) :- modifierT(X,'abstract').
adornmentFlag(X,2) :- modifierT(X,'final').
adornmentFlag(X,4) :- modifierT(X,'synchronized').
adornmentFlag(X,8) :- modifierT(X,'static').
adornmentFlag(X,512) :- constructor(X).

% TODO
% adornmentFlag(X,1024) :- deprecated(X).
% adornmentFlag(X,16) :- error(X).
% adornmentFlag(X,32) :- warning(X).
% adornmentFlag(X,64) :- runnable(X).
  
% 'override' adornment 
adornmentFlag(X,128) :- method(X),af_overrides(X,Ridden),nonAbstractMethod(Ridden),!.
 
% 'implements' adornment [only gets applied if the overrides adornment does not]
adornmentFlag(X,256) :- method(X),af_overrides(X,Ridden),not( nonAbstractMethod(Ridden) ),!.

%
% It appears this version of overrides executes faster for adornment flags, not for anything else...
% So I've used it here for convenience and speed.
af_overrides(M,MSuper) :-
	child(C,M),subTypePlus(Super,C),child(Super,MSuper),
	inheritableMethod(MSuper),
	strongLikeThis(M,MSuper).

