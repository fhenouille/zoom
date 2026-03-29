import { statisticsService } from '@/services/statisticsService';
import { AssistanceStatisticsResponse } from '@/types/statistics';
import {
  BarChartOutlined,
  CalendarOutlined,
  FilePdfOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Button, Card, DatePicker, Empty, Select, Space, Spin, Typography } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/fr';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { useEffect, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const { Title } = Typography;
const { RangePicker } = DatePicker;

// ─── Helpers pour le dessin de graphiques dans jsPDF ─────────────────────────

type ChartEntry = { date: string; Présentiel: number; Visio: number; Total: number };

const SERIES_KEYS = ['Présentiel', 'Visio', 'Total'] as const;
type SeriesKey = (typeof SERIES_KEYS)[number];

const SERIES_COLORS: Record<SeriesKey, [number, number, number]> = {
  Présentiel: [24, 144, 255],
  Visio: [82, 196, 26],
  Total: [114, 46, 209],
};

const drawPdfBarChart = (
  doc: jsPDF,
  data: ChartEntry[],
  x: number,
  y: number,
  w: number,
  h: number
) => {
  if (data.length === 0) return;
  const maxVal = Math.max(...data.flatMap((d) => SERIES_KEYS.map((s) => d[s])), 1);
  const padL = 15;
  const padB = 14;
  const padT = 5;
  const cw = w - padL;
  const ch = h - padB - padT;

  // Quadrillage horizontal + étiquettes axe Y
  const Y_TICKS = 5;
  for (let i = 0; i <= Y_TICKS; i++) {
    const val = Math.round((maxVal * i) / Y_TICKS);
    const ly = y + padT + ch - (ch * i) / Y_TICKS;
    doc.setDrawColor(220, 220, 220);
    doc.setLineWidth(0.2);
    doc.line(x + padL, ly, x + padL + cw, ly);
    doc.setFontSize(6);
    doc.setTextColor(100, 100, 100);
    doc.text(String(val), x + padL - 2, ly + 1.5, { align: 'right' });
  }

  // Axes
  doc.setDrawColor(0, 0, 0);
  doc.setLineWidth(0.4);
  doc.line(x + padL, y + padT, x + padL, y + padT + ch);
  doc.line(x + padL, y + padT + ch, x + padL + cw, y + padT + ch);

  // Barres
  const groupW = cw / data.length;
  const barW = (groupW * 0.72) / SERIES_KEYS.length;
  const groupPad = groupW * 0.14;

  data.forEach((d, i) => {
    SERIES_KEYS.forEach((s, j) => {
      const barH = (d[s] / maxVal) * ch;
      const bx = x + padL + i * groupW + groupPad + j * barW;
      const by = y + padT + ch - barH;
      const [r, g, b] = SERIES_COLORS[s];
      doc.setFillColor(r, g, b);
      doc.rect(bx, by, barW, Math.max(barH, 0.1), 'F');
    });
    doc.setFontSize(6);
    doc.setTextColor(80, 80, 80);
    doc.text(d.date, x + padL + (i + 0.5) * groupW, y + padT + ch + 5, { align: 'center' });
  });

  // Légende
  let lx = x + padL;
  const legendY = y + h;
  SERIES_KEYS.forEach((s) => {
    const [r, g, b] = SERIES_COLORS[s];
    doc.setFillColor(r, g, b);
    doc.rect(lx, legendY - 3, 4, 3, 'F');
    doc.setFontSize(8);
    doc.setTextColor(0, 0, 0);
    doc.text(s, lx + 5, legendY);
    lx += doc.getTextWidth(s) + 12;
  });
};

const drawPdfLineChart = (
  doc: jsPDF,
  data: ChartEntry[],
  x: number,
  y: number,
  w: number,
  h: number
) => {
  if (data.length === 0) return;
  const maxVal = Math.max(...data.flatMap((d) => SERIES_KEYS.map((s) => d[s])), 1);
  const padL = 15;
  const padB = 14;
  const padT = 5;
  const cw = w - padL;
  const ch = h - padB - padT;

  // Quadrillage horizontal + étiquettes axe Y
  const Y_TICKS = 5;
  for (let i = 0; i <= Y_TICKS; i++) {
    const val = Math.round((maxVal * i) / Y_TICKS);
    const ly = y + padT + ch - (ch * i) / Y_TICKS;
    doc.setDrawColor(220, 220, 220);
    doc.setLineWidth(0.2);
    doc.line(x + padL, ly, x + padL + cw, ly);
    doc.setFontSize(6);
    doc.setTextColor(100, 100, 100);
    doc.text(String(val), x + padL - 2, ly + 1.5, { align: 'right' });
  }

  // Axes
  doc.setDrawColor(0, 0, 0);
  doc.setLineWidth(0.4);
  doc.line(x + padL, y + padT, x + padL, y + padT + ch);
  doc.line(x + padL, y + padT + ch, x + padL + cw, y + padT + ch);

  // Position X d'un point (gère le cas 1 point)
  const xPos = (i: number) =>
    data.length === 1 ? x + padL + cw / 2 : x + padL + (i / (data.length - 1)) * cw;

  // Courbes + points
  SERIES_KEYS.forEach((s) => {
    const [r, g, b] = SERIES_COLORS[s];
    doc.setDrawColor(r, g, b);
    doc.setLineWidth(0.7);
    for (let i = 0; i < data.length - 1; i++) {
      doc.line(
        xPos(i),
        y + padT + ch - (data[i][s] / maxVal) * ch,
        xPos(i + 1),
        y + padT + ch - (data[i + 1][s] / maxVal) * ch
      );
    }
    data.forEach((d, i) => {
      doc.setFillColor(r, g, b);
      doc.circle(xPos(i), y + padT + ch - (d[s] / maxVal) * ch, 1, 'F');
    });
  });

  // Étiquettes axe X
  data.forEach((d, i) => {
    doc.setFontSize(6);
    doc.setTextColor(80, 80, 80);
    doc.text(d.date, xPos(i), y + padT + ch + 5, { align: 'center' });
  });

  // Légende
  let lx = x + padL;
  const legendY = y + h;
  SERIES_KEYS.forEach((s) => {
    const [r, g, b] = SERIES_COLORS[s];
    doc.setFillColor(r, g, b);
    doc.setDrawColor(r, g, b);
    doc.setLineWidth(0.7);
    doc.line(lx, legendY - 1.5, lx + 6, legendY - 1.5);
    doc.circle(lx + 3, legendY - 1.5, 1, 'F');
    doc.setFontSize(8);
    doc.setTextColor(0, 0, 0);
    doc.text(s, lx + 8, legendY);
    lx += doc.getTextWidth(s) + 15;
  });
};

function Statistics() {
  const [isLoading, setIsLoading] = useState(false);
  const [statistics, setStatistics] = useState<AssistanceStatisticsResponse | null>(null);
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(30, 'days'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [isPdfExporting, setIsPdfExporting] = useState(false);

  // Formate une date ISO en "Samedi 28 mars 2026"
  const formatDateFull = (dateStr: string): string => {
    const formatted = dayjs(dateStr).locale('fr').format('dddd D MMMM YYYY');
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  };

  // Exporte un rapport PDF : tableau + histogramme + courbes (dessinés directement via jsPDF)
  const handleExportPdf = () => {
    if (!statistics || statistics.dailyStats.length === 0) return;
    setIsPdfExporting(true);
    try {
      const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
      const pageWidth = doc.internal.pageSize.getWidth();

      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(0);
      doc.text("Rapport de Statistiques d'Assistance", pageWidth / 2, 18, { align: 'center' });
      doc.setFontSize(11);
      doc.setFont('helvetica', 'normal');
      doc.text(
        `Période : ${startDate.format('DD/MM/YYYY')} - ${endDate.format('DD/MM/YYYY')}`,
        pageWidth / 2,
        27,
        { align: 'center' }
      );

      autoTable(doc, {
        head: [['Date', 'Présentiel', 'Visio', 'Total']],
        body: statistics.dailyStats.map((stat) => [
          formatDateFull(stat.date),
          stat.inPerson,
          stat.remote,
          stat.total,
        ]),
        startY: 35,
        tableWidth: 195,
        margin: { left: (pageWidth - 195) / 2, right: (pageWidth - 195) / 2 },
        styles: { fontSize: 10, halign: 'center' },
        headStyles: { fillColor: [24, 144, 255], fontStyle: 'bold', halign: 'center' },
        columnStyles: {
          0: { cellWidth: 90, halign: 'left' },
          1: { cellWidth: 35, halign: 'center' },
          2: { cellWidth: 35, halign: 'center' },
          3: { cellWidth: 35, halign: 'center' },
        },
      });

      const pdfData: ChartEntry[] = statistics.dailyStats.map((stat) => ({
        date: dayjs(stat.date).format('DD/MM'),
        Présentiel: stat.inPerson,
        Visio: stat.remote,
        Total: stat.total,
      }));

      doc.addPage();
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(0);
      doc.text('Histogramme', pageWidth / 2, 15, { align: 'center' });
      drawPdfBarChart(doc, pdfData, 10, 22, pageWidth - 20, 150);

      doc.addPage();
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(0);
      doc.text('Courbes', pageWidth / 2, 15, { align: 'center' });
      drawPdfLineChart(doc, pdfData, 10, 22, pageWidth - 20, 150);

      doc.save(
        `rapport-assistance-${startDate.format('YYYY-MM-DD')}_${endDate.format('YYYY-MM-DD')}.pdf`
      );
    } finally {
      setIsPdfExporting(false);
    }
  };

  // Charge les statistiques
  const loadStatistics = async () => {
    setIsLoading(true);
    try {
      const data = await statisticsService.getAssistanceStatistics(
        startDate.format('YYYY-MM-DDTHH:mm:ss'),
        endDate.format('YYYY-MM-DDTHH:mm:ss')
      );
      setStatistics(data);
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error);
      setStatistics(null);
    } finally {
      setIsLoading(false);
    }
  };

  // Charge les statistiques au montage et quand les dates changent
  useEffect(() => {
    loadStatistics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [startDate, endDate]);

  // Gère le changement de période
  const handleDateRangeChange = (dates: [Dayjs | null, Dayjs | null] | null) => {
    if (dates?.[0] && dates?.[1]) {
      setStartDate(dates[0]);
      setEndDate(dates[1]);
    }
  };

  // Prépare les données pour le graphique
  const chartData =
    statistics?.dailyStats.map((stat) => ({
      date: dayjs(stat.date).format('DD/MM'),
      Présentiel: stat.inPerson,
      Visio: stat.remote,
      Total: stat.total,
    })) || [];

  // Calcule les moyennes
  const totalInPerson = statistics?.dailyStats.reduce((sum, stat) => sum + stat.inPerson, 0) || 0;
  const totalRemote = statistics?.dailyStats.reduce((sum, stat) => sum + stat.remote, 0) || 0;
  const totalAssistance = totalInPerson + totalRemote;

  // Nombre de jours avec des données
  const daysWithData = statistics?.dailyStats.length || 1;

  // Calcul des moyennes
  const avgInPerson = Math.round(totalInPerson / daysWithData);
  const avgRemote = Math.round(totalRemote / daysWithData);
  const avgTotal = Math.round(totalAssistance / daysWithData);

  // Détermine le contenu à afficher
  const renderChartContent = () => {
    if (isLoading) {
      return (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <p style={{ marginTop: '16px' }}>Chargement des statistiques...</p>
        </div>
      );
    }

    if (!statistics || statistics.dailyStats.length === 0) {
      return (
        <Empty
          description="Aucune donnée sauvegardée sur cette période"
          style={{ padding: '50px' }}
        />
      );
    }

    return (
      <div>
        <Title level={4}>Assistance par Jour</Title>
        <ResponsiveContainer width="100%" height={400}>
          {chartType === 'bar' ? (
            <BarChart
              data={chartData}
              margin={{
                top: 20,
                right: 30,
                left: 20,
                bottom: 5,
              }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="Présentiel" fill="#1890ff" />
              <Bar dataKey="Visio" fill="#52c41a" />
              <Bar dataKey="Total" fill="#722ed1" />
            </BarChart>
          ) : (
            <LineChart
              data={chartData}
              margin={{
                top: 20,
                right: 30,
                left: 20,
                bottom: 5,
              }}
            >
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="Présentiel"
                stroke="#1890ff"
                strokeWidth={2}
                dot={{ fill: '#1890ff' }}
              />
              <Line
                type="monotone"
                dataKey="Visio"
                stroke="#52c41a"
                strokeWidth={2}
                dot={{ fill: '#52c41a' }}
              />
              <Line
                type="monotone"
                dataKey="Total"
                stroke="#722ed1"
                strokeWidth={2}
                dot={{ fill: '#722ed1' }}
              />
            </LineChart>
          )}
        </ResponsiveContainer>
      </div>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>
        <BarChartOutlined /> Statistiques d'Assistance
      </Title>

      {/* Filtres de date */}
      <Card style={{ marginBottom: '24px' }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Space wrap>
            <CalendarOutlined />
            <span>Période :</span>
            <RangePicker
              value={[startDate, endDate]}
              onChange={handleDateRangeChange}
              format="DD/MM/YYYY"
              placeholder={['Date de début', 'Date de fin']}
            />
            <Select
              value={chartType}
              onChange={setChartType}
              style={{ width: 150 }}
              options={[
                { value: 'bar', label: 'Histogramme' },
                { value: 'line', label: 'Courbes' },
              ]}
            />
            <Button icon={<ReloadOutlined />} onClick={loadStatistics}>
              Actualiser
            </Button>
            {statistics && statistics.dailyStats.length > 0 && (
              <Button
                icon={<FilePdfOutlined />}
                type="primary"
                onClick={handleExportPdf}
                loading={isPdfExporting}
              >
                Exporter PDF
              </Button>
            )}
          </Space>
        </Space>
      </Card>

      {/* Statistiques globales */}
      {statistics && statistics.dailyStats.length > 0 && (
        <Card style={{ marginBottom: '24px' }}>
          <Space size="large">
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Présentiel</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
                {avgInPerson}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Visio</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#52c41a' }}>
                {avgRemote}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '14px', color: '#666' }}>Moyenne Totale</div>
              <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{avgTotal}</div>
            </div>
          </Space>
        </Card>
      )}

      {/* Graphique */}
      <Card>{renderChartContent()}</Card>
    </div>
  );
}

export default Statistics;
