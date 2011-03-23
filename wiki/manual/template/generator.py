#
#
# Copyright 2009-2010 Streamsource AB
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# We use this helper function to format the menu.
from statwiki import format_text

# Configuration. Set the values in your wiki's config file's [generator] section.
_name = genconfig.get('name', u'Wiki Name')
_title1 = genconfig.get('gray_title', u'Stat')
_title2 = genconfig.get('orange_title', u'Wiki')
_titlesep = u'&nbsp;' * int(genconfig.get('title_sep', '0'))
_slogan = genconfig.get('slogan', u'a static wiki')
_menu = genconfig.get('menu', u'')
_year = genconfig.get('year', u'2009')
_by = genconfig.get('by', u'Your Name')
_url = genconfig.get('statwiki_url', u'http://code.google.com/p/statwiki/')
_cseid = genconfig.get('cse_id', u'')

# Header.
out.write(u'''<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; CHARSET=utf-8">
    <link rel="shortcut icon" href="template/favicon.ico" type="image/vnd.microsoft.icon">
    <title>%(pagename)s - %(_name)s</title>
    <link rel="stylesheet" type="text/css" href="template/default.css">
  </head>
  <body>
    <div id="wrap">
      <div id="header">
        <h1 id="logo"><span class="blue">%(_title1)s</span>%(_titlesep)s%(_title2)s</h1>
        <h2 id="slogan">%(_slogan)s</h2>''' % globals())
if _cseid:
    out.write(u'''        <form action="http://www.google.com/cse" class="searchform" id="cse-search-box">
         <p>
          <input type="hidden" name="cx" value="%(_cseid)s">
          <input type="hidden" name="ie" value="UTF-8">
          <input type="text" name="q" size="31" class="textbox">
          <input type="submit" name="sa" value="Search" class="button">
         </p>
        </form>
        <script type="text/javascript" src="http://www.google.com/coop/cse/brand?form=cse-search-box&lang=en"></script>
    ''' % globals())
out.write(u'''      </div>
      <div id="menu">''')

# Create the menu bar.
if _menu:
    out.write(format_text(_menu.replace(u'[', u'\n * ['), pagename))

out.write(u'''      </div>
      <div id="content-wrap">''')

# Allow %(...)s formatting in the content if enabled per-page.
if globals().get('enable_content_formatting', False):
    content.wikitext %= globals()

# Insert the content.
if 'sidebar' in globals():
    # With sidebar.
    out.write(u'''<div id="sidebar">%(sidebar)s</div>
        <div id="mainbar">%(content)s</div>''' % globals())
else:
    # Without sidebar.
    out.write(u'<div id="main">%(content)s</div>' % globals())

# Footer.
modify_time = modify_time.replace(u' ', u'&nbsp;')
_by = _by.replace(u' ', u'&nbsp;')
out.write(u'''      </div>
    </div>
    <div id="footer">
      Updated&nbsp;at&nbsp;<strong>%(modify_time)s</strong>&nbsp;|&nbsp;&copy;&nbsp;Copyright&nbsp;%(_year)s&nbsp;<strong>%(_by)s</strong>&nbsp;|&nbsp;Powered&nbsp;by&nbsp;<a href="%(_url)s">StatWiki</a>&nbsp;|&nbsp;Design&nbsp;by&nbsp;<a href="http://www.styleshout.com/">styleshout</a>&nbsp;|&nbsp;Valid&nbsp;<a href="http://validator.w3.org/check/referer">HTML</a>&nbsp;and&nbsp;<a href="http://jigsaw.w3.org/css-validator/check/referer">CSS</a>
    </div>
  </body>
</html>''' % globals())
