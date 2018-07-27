#!/usr/bin/env python

import os, sys



if __name__ == '__main__':
    for i in xrange(616):
        os.system('mv %d.code.* backwards/' % i)
