Get Spectra using list of Gene accessions
==========================================

Swagger
-------
We recommend not to use browser for this as the amount of data could be really huge


Curl
-----
.. code-block:: bash

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByGeneAccessions" -H "accept: */*" -H "Content-Type: application/json" -d '["ENSG00000183785.15","TUBA4A","TUBA8"]'

   OR

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByGeneAccessions" -H "accept: */*" -H "Content-Type: application/json" -d '["ENSG00000183785.15","TUBA4A","TUBA8"]'


Python sample code
------------------

Using SSEs
***********

.. note::
   pip install sseclient-py

.. code-block:: python

   from sseclient import SSEClient   #pip install sseclient-py
   import requests

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByGeneAccessions'
   headers = {"Content-Type": "application/json"}
   data = '["ENSG00000183785.15","TUBA4A","TUBA8"]'

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

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByGeneAccessions'
   headers = {"Content-Type": "application/json"}
   data = '["ENSG00000183785.15","TUBA4A","TUBA8"]'

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
