BEGIN                 { nodebug = 0;
                        print"#pragma GCC diagnostic ignored \"-Wsign-compare\"";
                        print"#pragma GCC diagnostic ignored \"-Wstrict-aliasing\"";
			print"#include \"config.h\"";
			print"#include <stdio.h>";
			print"#include \"raslib/error.hh\"";
			print"#include \"raslib/rminit.hh\"";
			}
/rpcshutdown/         { nodebug = 1; print $0; next; }
/^}/                  { nodebug = 0; print $0; next; }
nodebug==1            { print $0; next; }
/static.*clnt_res;/    { print $0; print "\tenum clnt_stat stat;\n"; next; }
/if.*clnt_call/       { x=$0;
                        sub(/if *\( *clnt_call/, "if ( (stat = clnt_call", x);
                        sub(/\) *!= *RPC_SUCCESS *\)/, ") ) != RPC_SUCCESS )", x); 
                        print x;
                        next;
                      }
/\) *!= *RPC_SUCCESS/ { x=$0; 
                        sub(/\) *!= *RPC_SUCCESS *\)/, ") ) != RPC_SUCCESS )", x);
                        print x;
                        next;
                      }
/return *\(NULL\)/	{
			print "\t\tclnt_perrno( stat );";
			print $0;
			print "\t\t}";
			
			print "\tif (*((u_short*)&clnt_res) == 42)";
			print "\t\t{";
			print "\t\tGetExtendedErrorInfo* result = NULL;";
			print "\t\tint dummy;";
			print "\t\tint counter = 0;";
			print "\t\twhile (!(result = rpcgeterrorinfo_1(&dummy, clnt)) && (counter < RMInit::rpcMaxRetry))";
			print "\t\t\t{";
			print "\t\t\tcounter++;";
			print "\t\t\t}";
			print "\t\tr_Error* t = NULL, e;";
			print "\t\tif (counter == RMInit::rpcMaxRetry)";
			print "\t\t\tt = new r_Error(RPCCOMMUNICATIONFAILURE);";
			print "\t\telse";
			print "\t\t\tt = r_Error::getAnyError(result->errorText);";
			print "\t\te=*t;";
			print "\t\tdelete t;";
			print "\t\tthrow e;";
			next;
			}

                      { print $0; }

END                     {
                        print"#pragma GCC diagnostic warning \"-Wsign-compare\"";
                        print"#pragma GCC diagnostic warning \"-Wstrict-aliasing\"";
                        }