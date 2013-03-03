#!/usr/bin/python
# -*- coding: utf-8 -*-
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

try:
    import atexit
    import cmd
    import json
    import logging
    import os
    import pdb
    import re
    import shlex
    import sys

    from urllib2 import HTTPError, URLError
    from httplib import BadStatusLine

    from config import __version__, config_file
    from config import precached_verbs, read_config, write_config
    from printer import monkeyprint
    from requester import monkeyrequest

    from prettytable import PrettyTable
    from marvin.cloudstackConnection import cloudConnection
    from marvin.cloudstackException import cloudstackAPIException
    from marvin.cloudstackAPI import *
    from marvin import cloudstackAPI
except ImportError, e:
    print "Import error in %s : %s" % (__name__, e)
    import sys
    sys.exit()

# Fix autocompletion issue, can be put in .pythonstartup
try:
    import readline
except ImportError, e:
    print "Module readline not found, autocompletions will fail", e
else:
    import rlcompleter
    if 'libedit' in readline.__doc__:
        readline.parse_and_bind("bind ^I rl_complete")
    else:
        readline.parse_and_bind("tab: complete")

log_fmt = '%(asctime)s - %(filename)s:%(lineno)s - [%(levelname)s] %(message)s'
logger = logging.getLogger(__name__)
completions = cloudstackAPI.__all__


class CloudMonkeyShell(cmd.Cmd, object):
    intro = ("☁ Apache CloudStack 🐵 cloudmonkey " + __version__ +
             ". Type help or ? to list commands.\n")
    ruler = "="
    apicache = {}
    # datastructure {'verb': {cmd': ['api', [params], doc, required=[]]}}
    cache_verbs = precached_verbs
    config_options = []

    def __init__(self, pname, verbs):
        self.program_name = pname
        self.verbs = verbs

        self.config_options = read_config(self.get_attr, self.set_attr)
        self.prompt = self.prompt.strip() + " "  # Cosmetic fix for prompt

        logging.basicConfig(filename=self.log_file,
                            level=logging.DEBUG, format=log_fmt)
        logger.debug("Loaded config fields:\n%s" % map(lambda x: "%s=%s" %
                                                       (x, getattr(self, x)),
                                                       self.config_options))
        cmd.Cmd.__init__(self)

        try:
            if os.path.exists(self.history_file):
                readline.read_history_file(self.history_file)
            atexit.register(readline.write_history_file, self.history_file)
        except IOError:
            monkeyprint("Error: history support")

    def get_attr(self, field):
        return getattr(self, field)

    def set_attr(self, field, value):
        return setattr(self, field, value)

    def emptyline(self):
        pass

    def cmdloop(self, intro=None):
        print self.intro
        while True:
            try:
                super(CloudMonkeyShell, self).cmdloop(intro="")
                self.postloop()
            except KeyboardInterrupt:
                print("^C")

    def monkeyprint(self, *args):
        monkeyprint((self.color == 'true'), *args)

    def print_result(self, result, result_filter=None):
        if result is None or len(result) == 0:
            return

        def printer_helper(printer, toprow):
            if printer:
                self.monkeyprint(printer)
            return PrettyTable(toprow)

        def print_result_tabular(result, result_filter=None):
            toprow = None
            printer = None
            for node in result:
                if toprow != node.keys():
                    if result_filter is not None and len(result_filter) != 0:
                        commonkeys = filter(lambda x: x in node.keys(),
                                            result_filter)
                        if commonkeys != toprow:
                            toprow = commonkeys
                            printer = printer_helper(printer, toprow)
                    else:
                        toprow = node.keys()
                        printer = printer_helper(printer, toprow)
                row = map(lambda x: node[x], toprow)
                if printer and row:
                    printer.add_row(row)
            if printer:
                self.monkeyprint(printer)

        def print_result_as_dict(result, result_filter=None):
            for key in sorted(result.keys(), key=lambda x:
                              x not in ['id', 'count', 'name'] and x):
                if not (isinstance(result[key], list) or
                        isinstance(result[key], dict)):
                    self.monkeyprint("%s = %s" % (key, result[key]))
                else:
                    self.monkeyprint(key + ":")
                    self.print_result(result[key], result_filter)

        def print_result_as_list(result, result_filter=None):
            for node in result:
                # Tabular print if it's a list of dict and tabularize is true
                if isinstance(node, dict) and self.tabularize == 'true':
                    print_result_tabular(result, result_filter)
                    break
                self.print_result(node)
                if len(result) > 1:
                    self.monkeyprint(self.ruler * 80)

        if isinstance(result, dict):
            print_result_as_dict(result, result_filter)
        elif isinstance(result, list):
            print_result_as_list(result, result_filter)
        elif isinstance(result, str):
            print result
        elif not (str(result) is None):
            self.monkeyprint(result)

    def make_request(self, command, args={}, isasync=False):
        response, error = monkeyrequest(command, args, isasync,
                                        self.asyncblock, logger,
                                        self.host, self.port,
                                        self.apikey, self.secretkey,
                                        self.timeout, self.protocol, self.path)
        if error is not None:
            self.monkeyprint(error)
        return response

    def default(self, args):
        if self.pipe_runner(args):
            return

        lexp = shlex.shlex(args.strip())
        lexp.whitespace = " "
        lexp.whitespace_split = True
        lexp.posix = True
        args = []
        while True:
            next_val = lexp.next()
            if next_val is None:
                break
            args.append(next_val)
        api_name = args[0]

        args_dict = dict(map(lambda x: [x.partition("=")[0],
                                        x.partition("=")[2]],
                             args[1:])[x] for x in range(len(args) - 1))
        field_filter = None
        if 'filter' in args_dict:
            field_filter = filter(lambda x: x is not '',
                                  map(lambda x: x.strip(),
                                      args_dict.pop('filter').split(',')))

        for attribute in args_dict.keys():
            setattr(api_cmd, attribute, args_dict[attribute])

        #command = api_cmd()
        #missing_args = filter(lambda x: x not in args_dict.keys(),
        #                      command.required)

        #if len(missing_args) > 0:
        #    self.monkeyprint("Missing arguments: ", ' '.join(missing_args))
        #    return

        isAsync = False
        #if "isAsync" in dir(command):
        #    isAsync = (command.isAsync == "true")

        result = self.make_request(api_name, args_dict, isAsync)
        if result is None:
            return
        try:
            responsekeys = filter(lambda x: 'response' in x, result.keys())
            for responsekey in responsekeys:
                self.print_result(result[responsekey], field_filter)
            print
        except Exception as e:
            self.monkeyprint("🙈  Error on parsing and printing", e)

    def completedefault(self, text, line, begidx, endidx):
        partitions = line.partition(" ")
        verb = partitions[0].strip()
        rline = partitions[2].lstrip().partition(" ")
        subject = rline[0]
        separator = rline[1]
        params = rline[2].lstrip()

        if verb not in self.verbs:
            return []

        autocompletions = []
        search_string = ""

        if separator != " ":   # Complete verb subjects
            autocompletions = self.cache_verbs[verb].keys()
            search_string = subject
        else:                  # Complete subject params
            autocompletions = map(lambda x: x + "=",
                                  self.cache_verbs[verb][subject][1])
            search_string = text

        if self.tabularize == "true" and subject != "":
            autocompletions.append("filter=")
        return [s for s in autocompletions if s.startswith(search_string)]

    def do_sync(self, args):
        """
        Asks cloudmonkey to discovery and sync apis available on user specified
        CloudStack host server which has the API discovery plugin, on failure
        it rollbacks last datastore or api precached datastore.
        """
        response = self.make_request("listApis")
        f = open('test.json', "w")
        f.write(json.dumps(response))
        f.close()

    def do_api(self, args):
        """
        Make raw api calls. Syntax: api <apiName> <args>=<values>.

        Example:
        api listAccount listall=true
        """
        if len(args) > 0:
            return self.default(args)
        else:
            self.monkeyprint("Please use a valid syntax")

    def complete_api(self, text, line, begidx, endidx):
        mline = line.partition(" ")[2]
        offs = len(mline) - len(text)
        return [s[offs:] for s in completions if s.startswith(mline)]

    def do_set(self, args):
        """
        Set config for cloudmonkey. For example, options can be:
        host, port, apikey, secretkey, log_file, history_file
        You may also edit your ~/.cloudmonkey_config instead of using set.

        Example:
        set host 192.168.56.2
        set prompt 🐵 cloudmonkey>
        set log_file /var/log/cloudmonkey.log
        """
        args = args.strip().partition(" ")
        key, value = (args[0], args[2])
        setattr(self, key, value)  # keys and attributes should have same names
        self.prompt = self.prompt.strip() + " "  # prompt fix
        write_config(self.get_attr)

    def complete_set(self, text, line, begidx, endidx):
        mline = line.partition(" ")[2]
        offs = len(mline) - len(text)
        return [s[offs:] for s in self.config_options
                if s.startswith(mline)]

    def pipe_runner(self, args):
        if args.find(' |') > -1:
            pname = self.program_name
            if '.py' in pname:
                pname = "python " + pname
            self.do_shell("%s %s" % (pname, args))
            return True
        return False

    def do_shell(self, args):
        """
        Execute shell commands using shell <command> or !<command>

        Example:
        !ls
        shell ls
        !for((i=0; i<10; i++)); do cloudmonkey create user account=admin \
            email=test@test.tt firstname=user$i lastname=user$i \
            password=password username=user$i; done
        """
        os.system(args)

    def do_help(self, args):
        """
        Show help docs for various topics

        Example:
        help list
        help list users
        ?list
        ?list users
        """
        fields = args.partition(" ")
        if fields[2] == "":
            cmd.Cmd.do_help(self, args)
        else:
            verb = fields[0]
            subject = fields[2].partition(" ")[0]

            if subject in self.cache_verbs[verb]:
                self.monkeyprint(self.cache_verbs[verb][subject][2])
            else:
                self.monkeyprint("Error: no such api (%s) on %s" %
                                 (subject, verb))

    def complete_help(self, text, line, begidx, endidx):
        fields = line.partition(" ")
        subfields = fields[2].partition(" ")

        if subfields[1] != " ":
            return cmd.Cmd.complete_help(self, text, line, begidx, endidx)
        else:
            line = fields[2]
            text = subfields[2]
            return self.completedefault(text, line, begidx, endidx)

    def do_exit(self, args):
        """
        Quit CloudMonkey CLI
        """
        return self.do_quit(args)

    def do_quit(self, args):
        """
        Quit CloudMonkey CLI
        """
        self.monkeyprint("Bye!")
        return self.do_EOF(args)

    def do_EOF(self, args):
        """
        Quit on Ctrl+d or EOF
        """
        sys.exit()


def main():
    pattern = re.compile("[A-Z]")
    verbs = list(set([x[:pattern.search(x).start()] for x in completions
                 if pattern.search(x) is not None]).difference(['cloudstack']))
    for verb in verbs:
        def add_grammar(verb):
            def grammar_closure(self, args):
                if self.pipe_runner("%s %s" % (verb, args)):
                    return
                try:
                    args_partition = args.partition(" ")
                    res = self.cache_verbs[verb][args_partition[0]]
                    cmd = res[0]
                    helpdoc = res[2]
                    args = args_partition[2]
                except KeyError, e:
                    self.monkeyprint("Error: invalid %s api arg" % verb, e)
                    return
                if ' --help' in args or ' -h' in args:
                    self.monkeyprint(helpdoc)
                    return
                self.default("%s %s" % (cmd, args))
            return grammar_closure

        grammar_handler = add_grammar(verb)
        grammar_handler.__doc__ = "%ss resources" % verb.capitalize()
        grammar_handler.__name__ = 'do_' + verb
        setattr(CloudMonkeyShell, grammar_handler.__name__, grammar_handler)

    shell = CloudMonkeyShell(sys.argv[0], verbs)
    if len(sys.argv) > 1:
        shell.onecmd(' '.join(sys.argv[1:]))
    else:
        shell.cmdloop()

if __name__ == "__main__":
    main()