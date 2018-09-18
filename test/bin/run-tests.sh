#!/bin/bash
#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

if [ "$#" -eq 0 ]; then
    declare -a arr=("htmlunitaware" "servlet30" "servlet31" "servlet40" "javaee6web" "javaee6" "javaee7" "javaee8")
    printf '\nNo tests specified as command arguments, using default set\n'
else
    declare -a arr=("$@")
fi

printf '\n Running tests for: \n\n'
printf '* %s\n' "${arr[@]}"

top_dir="$(pwd)"

printf '\n Running tests from dir %s' "$top_dir"

for i in "${arr[@]}"
do

   cd $i

   if [ "$exit_code" -ne "0" ]; then
     printf "\n\n\n\n EXITING BECAUSE OF FAILURES. SEE ABOVE! \n\n\n\n"
     exit $exit_code
   fi

   printf "\n\n\n\n\n **************************************  \n Descending into $i \n **************************************  \n\n\n\n\n\n"

   $top_dir/bin/test-glassfish-default.sh

   exit_code=$?

   cd $top_dir

   printf "\n\n\n\n\n **************************************  \n Finished testing $i \n **************************************  \n\n\n\n\n\n"

   printf 'Back at %s\n' "$(pwd)"

   if [ "$exit_code" -ne "0" ]; then
     printf "\n\n\n\n EXITING BECAUSE OF FAILURES. SEE ABOVE! \n\n\n\n"
     exit $exit_code
   fi
done

