Search by one or more filters
==========================================

Swagger
-------
https://www.ebi.ac.uk/pride/multiomics/ws/swagger-ui/index.html?url=/pride/multiomics/ws/api-docs&configUrl=/pride/multiomics/ws/api-docs/swagger-config#/Spectra/findByGenericRequest

To get just total count : https://www.ebi.ac.uk/pride/multiomics/ws/swagger-ui/index.html?url=/pride/multiomics/ws/api-docs&configUrl=/pride/multiomics/ws/api-docs/swagger-config#/Spectra/findByGenericRequestCount

Sample request payloads
------------------------

sample

.. code-block:: JSON

  {
   "peptideSequenceRegex": "AQLG*",
   "ptm": {
     "ptmKey": "name",
     "ptmValue": "iTRAQ4plex"
   },
    "proteinAccessions": ["P40227", "ENSP00000352019.2"],
    "geneAccessions": ["ENST00000335503.3", "CCT6A"]
  }

.. warning::
    Any one filter is mandatory i.e., either 'peptideSequenceRegex' or 'ptm' or 'proteinAccessions' or 'geneAccessions'

.. warning::
   'ptmKey' should be one of these: 'name, accession, mass' and 'ptmValue' should be it's corresponding value

.. warning::
   'peptideSequenceRegex' parameter should contain at-least 4 valid characters

   valid: AS*DF, ASDF* etc.,

   invalid: AS*F, ASF* etc.,


Curl
-----
.. code-block:: bash

   SSE: curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/see/findByGenericRequest" -H "accept: */*" -H "Content-Type: application/json" -d '{"peptideSequenceRegex":"AQLG*","ptm":{"ptmKey":"name","ptmValue":"iTRAQ4plex"},"proteinAccessions":["P40227","ENSP00000352019.2"],"geneAccessions":["ENST00000335503.3","CCT6A"]}'

   OR

   Streams: curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByGenericRequest" -H "accept: */*" -H "Content-Type: application/json" -d '{"peptideSequenceRegex":"AQLG*","ptm":{"ptmKey":"name","ptmValue":"iTRAQ4plex"},"proteinAccessions":["P40227","ENSP00000352019.2"],"geneAccessions":["ENST00000335503.3","CCT6A"]}'


Python sample code
------------------

Using SSEs
***********

.. note::
   pip install sseclient-py

.. code-block:: python

   from sseclient import SSEClient   #pip install sseclient-py
   import requests

   url = 'curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/see/findByGenericRequest'
   headers = {"Content-Type": "application/json"}
   data = '{"peptideSequenceRegex":"AQLG*","ptm":{"ptmKey":"name","ptmValue":"iTRAQ4plex"},"proteinAccessions":["P40227","ENSP00000352019.2"],"geneAccessions":["ENST00000335503.3","CCT6A"]}'

   def main():
       response = requests.post(url, data=data, headers=headers, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       client = SSEClient(response)
       for event in client.events():
           if event.event.lower() == "spectrum":
               print(event.data)
           elif event.event.lower() == "done":
               client.close()
               break


   if __name__ == "__main__":
       main()

Using Stream
*************

.. code-block:: python

   import requests

   url = 'curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByGenericRequest'
   headers = {"Content-Type": "application/json"}
   data = '{"peptideSequenceRegex":"AQLG*","ptm":{"ptmKey":"name","ptmValue":"iTRAQ4plex"},"proteinAccessions":["P40227","ENSP00000352019.2"],"geneAccessions":["ENST00000335503.3","CCT6A"]}'

   def main1():
       response = requests.post(url, data=data, headers=headers, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       for line in response.iter_lines():
           if line:
               print(line)


   if __name__ == "__main__":
       main1()

